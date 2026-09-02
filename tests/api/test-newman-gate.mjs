import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { writeFailureSummary, writeNewmanStats } from "./newman-gate.mjs";

const tempRoot = fs.mkdtempSync(path.join(os.tmpdir(), "newman-gate-"));

function report(stats, failures = []) {
  const file = path.join(tempRoot, `report-${Date.now()}-${Math.random().toString(16).slice(2)}.json`);
  fs.writeFileSync(file, JSON.stringify({ run: { stats, failures } }));
  return file;
}

const passed = {
  iterations: { total: 1, pending: 0, failed: 0 },
  requests: { total: 82, pending: 0, failed: 0 },
  testScripts: { total: 82, pending: 0, failed: 0 },
  prerequestScripts: { total: 82, pending: 0, failed: 0 },
  assertions: { total: 163, pending: 0, failed: 0 },
};

const statsPath = path.join(tempRoot, "stats.json");
const summaryPath = path.join(tempRoot, "summary.md");

assert.equal(writeNewmanStats(report(passed), statsPath, 0), 0, "passing Newman must keep exit 0");
assert.equal(JSON.parse(fs.readFileSync(statsPath, "utf8")).assertions.total, 163);

const failedAssertions = structuredClone(passed);
failedAssertions.assertions.failed = 2;
assert.equal(writeNewmanStats(report(failedAssertions), statsPath, 0), 1, "assertion failures must force non-zero exit");

const failedRequests = structuredClone(passed);
failedRequests.requests.failed = 1;
assert.equal(writeNewmanStats(report(failedRequests), statsPath, 0), 1, "request failures must force non-zero exit");

assert.equal(
  writeNewmanStats(report(passed, [{ error: { message: "boom" } }]), statsPath, 0),
  1,
  "recorded Newman failures must force non-zero exit",
);

const missing = path.join(tempRoot, "missing-report.json");
assert.equal(writeNewmanStats(missing, statsPath, 0), 1, "missing report must force non-zero exit");
const missingPayload = JSON.parse(fs.readFileSync(statsPath, "utf8"));
assert.equal(missingPayload.failures, 1);
assert.match(missingPayload.error, /missing/);

writeFailureSummary(report(passed, [{
  source: { name: "MS-TOPIC-CREATE-001", request: { method: "POST", url: { raw: "http://interaction/api/topics" } } },
  error: { test: "Status code is 200", message: "expected 200 got 500" },
}]), summaryPath);
const summary = fs.readFileSync(summaryPath, "utf8");
assert.match(summary, /Failures: 1/);
assert.match(summary, /MS-TOPIC-CREATE-001/);
assert.match(summary, /POST http:\/\/interaction\/api\/topics/);

fs.rmSync(tempRoot, { recursive: true, force: true });
console.log("Newman failure gate tests: OK");
