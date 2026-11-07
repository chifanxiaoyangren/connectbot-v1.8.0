# ConnectBot v1.8.0 — Android 2.2.2 定制版

这是一个基于 **ConnectBot v1.8.0** 修改的 Android SSH 客户端，专门用于
**Android 2.2.2（API 8）** 设备，例如 Motorola ME722 / Milestone 2。

它不是当前 ConnectBot 主线仓库，而是一个可独立编译、安装和继续维护的旧版分支。

---

## 主要功能

| 功能 | 说明 |
|---|---|
| SSH / Telnet 终端 | 支持远程 Shell、端口转发、公钥认证、known hosts |
| Android 2.2.2 兼容 | 修复现代编译器和旧 Android API 差异 |
| ECDSA 主机密钥兼容 | Android 2.x 没有 ECDSA 时自动回退到 `ssh-rsa` |
| 记住密码 | 密码 AES 加密保存，下次连接同一主机自动登录 |
| 隐藏状态栏 | 在终端和主机列表隐藏系统状态栏，获得更多显示空间 |
| 字体大小 | 全局调整终端字体，6–24 pt |
| Kiosk / 桌面模式 | 可将 ConnectBot 主机列表设置为手机桌面，减少误触其他应用 |

---

## APK 安装

仓库根目录已经附带调试版 APK：

```text
connectbot-v180-debug.apk
```

SHA-256：

```text
873d68f0c3ae9089a3388a8d8c08a8787a05edcb80569968850ef8badd17d5d5
```

安装到已连接的设备：

```sh
adb install -r connectbot-v180-debug.apk
```

---

## 编译要求

| 项目 | 版本 |
|---|---|
| JDK | 17 |
| Gradle | 7.6.4（仓库已带 wrapper） |
| Android Gradle Plugin | 7.4.2 |
| compileSdk | 33 |
| minSdk | 4 |
| targetSdk | 15 |
| ABI | `armeabi`、`x86` |

首次编译前需要配置 Android SDK 路径。可以创建 `local.properties`：

```properties
sdk.dir=/path/to/android-sdk
```

然后执行：

```sh
./gradlew assembleDebug
```

APK 输出位置：

```text
build/outputs/apk/debug/connectbot-v180-debug.apk
```

---

## 记住密码

第一次连接主机时，在 `Password:` 输入框下方勾选：

```text
记住密码
```

登录成功后，密码会加密保存到本机。下次连接同一主机时会自动尝试，不再弹出密码框。

如果服务器密码变更，旧密码认证失败后会自动删除保存的密码，并重新提示输入。

手动删除保存的密码：

```text
终端菜单 → 忘记已保存的密码
```

### 安全说明

Android 2.2.2 没有硬件级 Keystore。这里的密码使用应用私有目录中的密钥进行 AES 加密，
可以防止普通应用读取，但**不能抵抗已经 root 的设备**。请只在可信设备上使用。

---

## 设置与界面

### 隐藏状态栏

```text
设置 → 用户界面 → 隐藏状态栏
```

开启后，终端和主机列表都会隐藏系统状态栏。

### 字体大小

```text
设置 → 用户界面 → 字体大小
```

可选 6、8、10、12、14、16、18、20、24 pt。修改后会应用到所有已保存主机，
当前已经打开的终端也会立即调整。

---

## Kiosk / 桌面模式

这个版本已经给 `HostListActivity` 增加了 `HOME` intent filter，可以把 ConnectBot
设置为 Android 桌面。

设置方法：

1. 解锁手机；
2. 按 Home 键；
3. 选择 ConnectBot；
4. 勾选“设为默认”。

之后按 Home 键会回到 ConnectBot 主机列表，不再显示其他应用图标。

### 退出 Kiosk

在 ConnectBot 主机列表按菜单键：

```text
菜单 → 系统设置 → 应用程序 → 管理应用程序 → ConnectBot → 清除默认值
```

然后按 Home 键选择原来的桌面。

### 限制

Android 2.2 没有现代屏幕固定 / Device Owner 机制，所以这不是安全级锁定。
通知栏、最近任务、系统设置仍可能离开 ConnectBot。隐藏状态栏只能减少误触。

---

## 相对 v1.8.0 的修改

### 构建兼容

- 使用现代 Gradle 构建旧版 Ant 项目。
- 修复 AAPT2 不接受的 `@+drawable` 写法。
- 替换现代 SDK 中已移除的：
  - `PixelXorXfermode`
  - `Canvas.CLIP_SAVE_FLAG`
  - `Notification.setLatestEventInfo`
- 保留旧版预编译 native 库：`armeabi`、`x86`。

### SSH 兼容

- Android 2.x 没有 `KeyFactory EC` 和 `SHA256withECDSA`。
- 客户端现在会检测 ECDSA 支持；不支持时只使用 `ssh-rsa` 等旧算法。
- 已验证可连接 OpenSSH 8.6 服务器。

### 密码保存

- hosts 数据库升级到版本 23。
- 新增 `password` BLOB 列。
- 密码通过 `Encryptor` / `PubkeyUtils` AES 加密保存。
- 支持普通 password 认证和 keyboard-interactive 中的密码提示。

### 桌面模式

- `HostListActivity` 支持作为 Home 应用。
- 作为 Home 时忽略返回键，避免误触退出。
- 菜单新增“系统设置”，方便清除默认桌面。

---

## 已知限制

- 仅包含 32 位 native ABI：`armeabi`、`x86`。
- Android 2.2.2 的 TLS/SSH 算法较旧，连接现代服务器时可能受服务器配置限制。
- 这是 debug 签名 APK，不是正式签名版本。
- 未对设备执行 root；系统预装应用无法通过本项目删除。

---

## 上游与许可

- 上游项目：[ConnectBot](https://github.com/connectbot/connectbot)
- 本仓库基于上游 tag：`v1.8.0`
- 许可证：Apache License 2.0
