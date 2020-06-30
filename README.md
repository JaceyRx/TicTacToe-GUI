## 概览
![客户端演示](https://github.com/JaceyRx/JGameServer/blob/master/doc/img/client.gif "客户端演示")

## 说明
 该项目为 [JGameServer](https://github.com/JaceyRx/JGameServer "JGameServer") 游戏服务器后端项目的测试客户端。请配合服务器端一起使用
 
 ## 快速启动
 ```
 1. clone https://github.com/JaceyRx/TicTacToe-GUI.git
 2. 进入项目目录，修改config.properties配置文件（修改服务器端IP地址）
 3. mvn install -DskipTest 编译项目
 4. 进入 target 目录，使用命令 java -Djava.awt.headless=false -jar TicTacToe-GUI.jar 启动项目
 ```
## Tips
 ```
 1. 开发环境是 JDK1.8 高于或低于该版本JDK可能会无法运行
 2. 请使用IDEA 打开项目
 ```
