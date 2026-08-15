# shizLite

基于 Shizuku 原理开发的轻量级 Android 系统优化工具。

## 特性

- **双激活方式**：仅保留无线调试激活与 Root 激活，精简冗余入口
- **模块系统**：支持自定义 `.slm` 模块扩展，每个模块独立运行、按需激活
- **双重授权**：应用授权 + 终端授权，持久化存储，可随时撤销
- **Material 3 设计**：蓝/青色主题，支持深色/浅色模式，底部导航栏
- **纯本地运行**：无联网功能，保护隐私
- **预置双模块**：
  - 系统优化大师（普通/Shizuku级）：清理缓存、动画缩放、冻结应用、查看系统属性
  - Root高级优化大师（Root级）：内核调优、深度清理、hosts修改、CPU调度、分辨率/DPI、SELinux、完整pm命令集

## 模块格式

`.slm` 文件本质为 ZIP 压缩包，结构如下：
```
module.zip/
├── module.json    # 元数据（名称、版本、作者、权限级别、入口脚本）
└── main.sh        # 入口可执行脚本
```

`module.json` 示例：
```json
{
  "id": "my_module",
  "name": "我的模块",
  "version": "1.0.0",
  "author": "作者",
  "description": "模块描述",
  "requiredLevel": "shizuku",
  "entryScript": "main.sh"
}
```

## 构建

```bash
./gradlew assembleRelease
```

或通过 GitHub Actions 自动构建。

## 致谢

本应用基于 [Shizuku](https://github.com/RikkaApps/Shizuku) 原理开发，遵循 GPL-3.0 协议。

## 许可证

GPL-3.0
