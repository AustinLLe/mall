### 镜像仓库
华为云容器镜像服务 SWR
组织名称 songguo

1. 使用password登录IAM 
   https://auth.huaweicloud.com/authui/login?id=hid_uqhqascbx4qdiuf

2. 容器镜像服务SWR-组织管理可以找到songguo组织以及已有镜像

3. 在容器镜像服务SWR-我的镜像-客户端上传-生成登录指令-长期有效登录指令中导入credentials文件得到长期有效登录指令![image-20260827195237931](C:\Users\Austi\AppData\Roaming\Typora\typora-user-images\image-20260827195237931.png)

4. 将代码build并推送至镜像仓库流程，上传后可在组织管理-镜像中查看镜像和镜像历史版本

   powershell

   ```
   ##用第 3 步复制的指令登录 SWR（以下为示例）
   
   docker login -u cn-north-4@HPUAFISS2TCGHDRZ93RC -p 9582208e213627524e49c3c1d27f64db36e0d1196ae0ec18fbefc107232cd79e swr.cn-north-4.myhuaweicloud.com
   
   ##打镜像tag（示例）
   
   $tag="test-20260827"
   
   ##分别构建前后端镜像并推送至SWR镜像仓库，写回本机 kustomization.yaml 的 newTag
   .\ops\push-swr.ps1 $tag backend
   .\ops\push-swr.ps1 $tag frontend
   
   ```

   ![image-20260827201211588](C:\Users\Austi\AppData\Roaming\Typora\typora-user-images\image-20260827201211588.png)

5. 把已更新的 YAML 拷到 ECS，ssh root@120.46.222.10 密码为mall@123

   powershell

   ```
   ##覆盖 ECS 上集群真正读取的那份 kustomization.yaml
   scp 本机项目存放目录\NewSecondMall\k8s\kustomization.yaml root@120.46.222.10:~/NewSecondMall/k8s/kustomization.yaml
   
   ```

6. SSH 上 ECS，让 Kubernetes 换成新镜像
   powershell

   ```
   ##登录云主机（k3s 装在这台机器上）
   ssh root@120.46.222.10
   
   登录后在 Linux 终端执行（不是本机 PowerShell）：
   ##确认版本号是否一致
   cd ~/NewSecondMall
   cat k8s/kustomization.yaml
   
   ##按 kustomization.yaml 部署
   sudo kubectl apply -k k8s/

   # mysql / backend / frontend 均为 Running 且 1/1 即成功
   sudo kubectl -n shop get pods
   ```

   