import fs from "node:fs";

export function emptyStat() {
  return { total: 0, pending: 0, failed: 0 };
}

export function writeFailureSummary(reportPath, summaryPath) {
  if (!fs.existsSync(reportPath)) {
    fs.writeFileSync(summaryPath, "# Microservices E2E failure summary\n\nnewman-report.json missing\n", "utf8");
    return;
  }
  const report = JSON.parse(fs.readFileSync(reportPath, "utf8"));
  const failures = report.run?.failures ?? [];
  const lines = [
    "# Microservices E2E failure summary",
    "",
    `Generated: ${new Date().toISOString()}`,
    `Failures: ${failures.length}`,
    "",
  ];
  for (const failure of failures) {
    const source = failure.source ?? {};
    const request = source.request ?? failure.parent?.request ?? {};
    const url = request.url?.raw ?? request.url ?? "unknown";
    lines.push(`## ${source.name ?? failure.error?.name ?? "Unknown failure"}`);
    lines.push(`- Request: ${request.method ?? "unknown"} ${url}`);
    lines.push(`- Assertion: ${failure.error?.test ?? failure.error?.message ?? "unknown"}`);
    lines.push(`- Actual: ${failure.error?.message ?? "No error message"}`);
    lines.push("");
  }
  if (failures.length === 0) lines.push("No Newman assertion failures were recorded. Check environment diagnostics.");
  fs.writeFileSync(summaryPath, `${lines.join("\n")}\n`, "utf8");
}

export function writeNewmanStats(reportPath, statsPath, exitCode) {
  let payload = {
    generatedAt: new Date().toISOString(),
    exitCode,
    iterations: emptyStat(),
    requests: emptyStat(),
    testScripts: emptyStat(),
    prerequestScripts: emptyStat(),
    assertions: emptyStat(),
    failures: 0,
  };
  if (fs.existsSync(reportPath)) {
    const report = JSON.parse(fs.readFileSync(reportPath, "utf8"));
    const stats = report.run?.stats ?? {};
    payload = {
      ...payload,
      iterations: stats.iterations ?? emptyStat(),
      requests: stats.requests ?? emptyStat(),
      testScripts: stats.testScripts ?? emptyStat(),
      prerequestScripts: stats.prerequestScripts ?? emptyStat(),
      assertions: stats.assertions ?? emptyStat(),
      failures: (report.run?.failures ?? []).length,
    };
  } else {
    payload.error = "newman-report.json missing";
    payload.failures = 1;
  }
  fs.writeFileSync(statsPath, `${JSON.stringify(payload, null, 2)}\n`);
  const assertionFailed = Number(payload.assertions?.failed || 0);
  const requestFailed = Number(payload.requests?.failed || 0);
  if (assertionFailed > 0 || requestFailed > 0 || payload.failures > 0) {
    return Math.max(exitCode || 0, 1);
  }
  return exitCode;
}
