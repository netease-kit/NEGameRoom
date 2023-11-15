网易云信为您提供开源的示例项目，您可以参考本文档快速跑通示例项目，体验游戏房的效果。

## 开发环境要求

在开始运行示例项目之前，请确保开发环境满足以下要求：
| 环境要求                                                        | 说明                                                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
|  iOS 版本  |  11.0 及以上的 iPhone 或者 iPad 真机   |
|  CPU 架构 | ARM64、ARMV7   |
| IDE | XCode   |
| 其他 | 安装 CocoaPods  |

## 前提条件

请确认您已完成以下操作：
- [已创建应用并获取AppKey](https://doc.yunxin.163.com/console/docs/TIzMDE4NTA?platform=console)
- [已开通相关能力](https://doc.yunxin.163.com/docs/TA3ODAzNjE/zQ4MTI0Njc?platformId=50616)
- 已配置 NERoom 的消息抄送地址（http://yiyong.netease.im/nemo/entertainmentLive/nim/notify），具体请联系网易云信技术支持



## 运行示例项目

::: note notice
- 游戏房的示例源码仅供开发者接入参考，实际应用开发场景中，请结合具体业务需求修改使用。

- 若您计划将源码用于生产环境，请确保应用正式上线前已经过全面测试，以免因兼容性等问题造成损失。

:::

  
1. 克隆[游戏房示例项目源码](https://github.com/netease-kit/NEGameRoom/tree/master/iOS)仓库至您本地工程。
    ::: note note
    示例项目源码请存放至全英文的路径下。
    :::

2. 打开终端，在 (VoiceRoomKit/NEGame/NEGameSample) Podfile 所在文件夹中执行如下命令进行安装：

    ```
    pod install 
    ```

3. 在 `VoiceRoomKit/NEGame/NEGameSample/NEGameSample/AppKey.swift` 中，替换您自己的 App Key 和 App Secret 。 
   

    ```
    // MARK: 请填写您的AppKey和AppSecret
    let APP_KEY: String = "your appkey" // 请填写应用对应的AppKey，可在云信控制台的“AppKey管理”页面获取
    let APP_SECRET: String = "your secret" // 请填写应用对应的AppSecret，可在云信控制台的“AppKey管理”页面获取

    // MARK: 如果您的AppKey为海外，填ture；如果您的AppKey为中国国内，填false
    let IS_OVERSEA = false

    // MARK: BASE_URL的默认地址仅用于跑通体验Demo，请勿用于正式产品上线。在产品上线前，请换为您自己实际的服务端地址
    let BASE_URL: String = "https://yiyong.netease.im" //云信派对服务端中国国内的体验地址
    let BASE_URL_OVERSEA: String = "http://yiyong-sg.netease.im"  //云信派对服务端海外的体验地址

    ```


    ::: note note
    - 获取 AppKey 和 AppSecret 的方法请参见<a href="https://doc.yunxin.163.com/console/docs/TIzMDE4NTA?platform=console#获取-appkey" target="_blank">创建应用并获取 AppKey</a>。
    - 配置文件中的 BASE_URL 地址 `http://yiyong.netease.im`为云信派对服务端体验地址，该地址仅用于体验 Demo，请勿用于生产环境。 您可以使用云信派对 Demo 体验 1 小时音视频通话。
    - 如果您的应用的 AppKey 为海外，`IS_OVERSEA` 的值请设置为 `ture`。
    :::


  

4. 运行工程。

    建议在真机上运行，不支持模拟器调试。





## 示例项目结构

```
┌── third_party
│  └─ lottie              // 动画组件源码文件夹
├─ OneOnOne
│  └─  NELoginSample       // 登录页面/功能封装文件夹  
├─ Party
│  └─  NESocialUIKit       // 通用UI层文件夹
├─ UIKit
│  └─  NEUIKit             // 通用分类文件夹
├─ VoiceRoomKit
│  ├─  NEVoiceRoomKit      // 语聊房基于NERoomKit封装组件文件夹
│  └─  NEVoiceRoomBaseUIKit    // 语聊房(业务，UI)层文件夹  
└─ NEGame
    └── NEGameKit         // 游戏房核心业务逻辑
        └──  NEGameUIKit       // 游戏房(业务，UI)层文件夹   
        
```
