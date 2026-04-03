<img src="doc/Nacos_Logo.png" width="400" />

# Nacos: Dynamic *Na*ming and *Co*nfiguration *S*ervice

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![CI](https://github.com/alibaba/nacos/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/alibaba/nacos/actions/workflows/ci.yml)
[![Gitter](https://badges.gitter.im/alibaba/nacos.svg)](https://gitter.im/alibaba/nacos?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Nacos Contribution](https://img.shields.io/badge/Nacos-Check%20Your%20Contribution-orange)](https://opensource.alibaba.com/contribution_leaderboard/details?projectValue=nacos)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/alibaba/nacos)

> An easy-to-use platform for dynamic service discovery, configuration management, and **AI agent management**. Build cloud-native applications and AI Agent applications easily.

---

## AI Registry - Unified Registration and Governance for AI Resources

Nacos 3.x introduces **AI Registry**, extending Nacos from microservices governance to AI resource governance. Built on a unified `AiResource` abstraction, it provides a general-purpose platform for registering, discovering, and managing any type of AI resource - with enterprise-grade security, version control, and publish pipelines. Prompt, MCP, A2A, Skills, and AgentSpec are supported today, and any future AI resource type can be managed through the same extensible framework.

### Supported Resource Types

| Resource | Description |
|----------|-------------|
| **Skills** | Register, share, and discover reusable AI agent skills across teams |
| **Prompt** | Register, version, and dynamically push prompt templates across environments |
| **MCP Server** | Register and discover [MCP](https://modelcontextprotocol.io/) servers with health checks and load balancing |
| **A2A Agent** | [Agent-to-Agent](https://google.github.io/A2A/) registration and discovery for multi-agent collaboration |
| **AgentSpec** | Register and manage agent specifications, packaging agent definitions as discoverable resources |

### Enterprise-Grade Governance

- **Security** - Full `@Secured` coverage with dedicated `SignType.AI`, role-based access control, resource visibility management (public/private), and force-publish restricted to admin
- **Version Management** - Unified lifecycle for all resources (`draft -> reviewing -> online <-> offline`), multi-version coexistence, label-based routing (e.g. `latest`), and fork from history for rollback
- **Publish Pipeline** - Pluggable review pipeline (SPI) with checkpoint-based auditing. Resources must pass all pipeline checks before going online, ensuring quality and compliance
- **Dynamic Push** - Built on Nacos Config's proven push mechanism. Changes propagate to clients in real-time without redeployment

---

## Quick Start

**macOS / Linux**
```bash
curl -fsSL https://nacos.io/nacos-installer.sh | bash
```

**Windows (PowerShell)**
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -Command "iwr -UseBasicParsing https://nacos.io/nacos-installer.ps1 | iex"
```

**Docker**
```bash
docker run --name nacos-standalone -e MODE=standalone -p 8848:8848 -p 9848:9848 -d nacos/nacos-server:latest
```

> For more options, see [Quick Start Guide](https://nacos.io/docs/latest/quickstart/quick-start/).

---

## Core Features

Nacos also provides essential capabilities for cloud-native microservices:

- **Service Discovery** - Register and discover services via DNS or HTTP, with real-time health checks
- **Dynamic Configuration** - Centralized configuration management across all environments
- **Dynamic DNS** - Weighted routing, load balancing, and flexible routing policies
- **Metadata Management** - Service dashboard for managing metadata, health, and metrics

Learn more: [Nacos Architecture & Principles](https://nacos.io/docs/ebook/kbyo6n/)

## Ecosystem Integration

- [Dubbo / gRPC](https://nacos.io/docs/latest/ecology/use-nacos-with-dubbo/)
- [Spring Cloud](https://nacos.io/docs/latest/ecology/use-nacos-with-spring-cloud/)
- [Kubernetes](https://nacos.io/docs/latest/quickstart/quick-start-kubernetes/)

## Documentation

- [Official Documentation](https://nacos.io/docs/latest/overview/)
- [GitHub Notices](https://github.com/alibaba/nacos/labels/notice)

## Contributing

We welcome contributions! See [CONTRIBUTING.md](./CONTRIBUTING.md) for guidelines.

**Good starting points:**
- [`good first issue`](https://github.com/alibaba/nacos/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22) - Perfect for newcomers
- [`contribution welcome`](https://github.com/alibaba/nacos/issues?q=is%3Aopen+is%3Aissue+label%3A%22contribution+welcome%22) - Help needed

## Related Projects

| Project | Description |
|---------|-------------|
| [nacos-group](https://github.com/nacos-group) | Ecosystem tools, SDKs, and extensions |
| [nacos-spring-project](https://github.com/nacos-group/nacos-spring-project) | Spring integration |
| [spring-cloud-alibaba](https://github.com/spring-cloud-incubator/spring-cloud-alibaba) | Spring Cloud integration |

## Community

- [Gitter](https://gitter.im/alibaba/nacos) - Community chat
- [Twitter](https://twitter.com/nacos2) - Latest news
- [Weibo](https://weibo.com/u/6574374908) - Chinese community
- [Segmentfault](https://segmentfault.com/t/nacos) - Q&A

**Mailing Lists:**
- users-nacos@googlegroups.com - General discussion
- dev-nacos@googlegroups.com - Developer discussion
- commits-nacos@googlegroups.com - Commit notifications

### WeChat & DingTalk Groups

| DingTalk Group | MCP Group | WeChat Group |
|:--------------:|:---------:|:------------:|
| <img src="https://cdn.nlark.com/yuque/0/2025/png/1577777/1750054497446-f834cba6-fa83-4421-b202-a0dc1d5cc28b.png" width="200" /> | <img src="https://cdn.nlark.com/yuque/0/2025/png/1577777/1750054500395-e271cbe4-2dd8-4723-8cd0-bd8a731b812a.png" width="200" /> | <img src="https://cdn.nlark.com/yuque/0/2025/png/1577777/1750054421702-a7d1421a-ab8e-42da-bc59-01b5d287b290.png" width="200" /> |

## Enterprise Service

For enterprise support or Alibaba Cloud MSE service: [Alibaba Cloud MSE](https://cn.aliyun.com/product/aliware/mse?spm=nacos-website.topbar.0.0.0)

## Download

- [Nacos Official Website](https://nacos.io/download/nacos-server)
- [GitHub Releases](https://github.com/alibaba/nacos/releases)

## Who is Using Nacos

These are only part of the companies using Nacos, for reference only. If you are using Nacos, please [add your company here](https://github.com/alibaba/nacos/issues/273) to tell us your scenario to make Nacos better.

<table>
<tr>
<td align="center"><img src="https://data.alibabagroup.com/ecms-files/886024452/296d05a1-c52a-4f5e-abf2-0d49d4c0d6b3.png" height="60" alt="Alibaba" /></td>
<td align="center"><img src="https://a.msstatic.com/huya/main/img/logo.png" height="60" alt="Huya" /></td>
<td align="center"><img src="https://v.icbc.com.cn/userfiles/Resources/ICBC/shouye/images/2017/logo.png" height="60" alt="ICBC" /></td>
<td align="center"><img src="https://pic2.iqiyipic.com/lequ/20220422/e7fe69c75e2541f2a931c9e538e2ab9d.jpg" height="60" alt="iQIYI" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1pwi9EwHqK1RjSZJnXXbNLpXa-479-59.png" height="60" alt="PingAn" /></td>
</tr>
<tr>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1MZWSEzDpK1RjSZFrXXa78VXa-269-69.png" height="60" alt="华夏信财" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1ebu.EAvoK1RjSZFwXXciCFXa-224-80.png" height="60" alt="贝壳找房" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1lxu7EBLoK1RjSZFuXXXn0XXa-409-74.png" height="60" alt="瑞安农商银行" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1L16eEzTpK1RjSZKPXXa3UpXa-302-50.png" height="60" alt="司法大数据" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1OigyDyLaK1RjSZFxXXamPFXa-168-70.png" height="60" alt="平行云" /></td>
</tr>
<tr>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1gJ4vIhTpK1RjSZR0XXbEwXXa-462-60.jpg" height="60" alt="甘肃紫光" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1DZWSEzDpK1RjSZFrXXa78VXa-240-62.png" height="60" alt="Acmedcare+" /></td>
<td align="center"><img src="https://user-images.githubusercontent.com/10215557/51593180-7563af00-1f2c-11e9-95b1-ec2c645d6a0b.png" height="60" alt="立思辰" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1zWW2EpYqK1RjSZLeXXbXppXa-262-81.png" height="60" alt="东家" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1G216EsbpK1RjSZFyXXX_qFXa-325-53.jpg" height="60" alt="南京28研究所" /></td>
</tr>
<tr>
<td align="center"><img src="https://p1.ifengimg.com/auto/image/2017/0922/auto_logo.png" height="60" alt="凤凰网汽车" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1DXerNgDqK1RjSZSyXXaxEVXa-333-103.png" height="60" alt="一点车" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1VfOANgHqK1RjSZFPXXcwapXa-313-40.png" height="60" alt="明传无线" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1lvCyNhTpK1RjSZFMXXbG_VXa-130-60.png" height="60" alt="妙优车" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1kY9qNgTqK1RjSZPhXXXfOFXa-120-50.png" height="60" alt="蜂巢" /></td>
</tr>
<tr>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1G.GBNbrpK1RjSZTEXXcWAVXa-234-65.png" height="60" alt="华存数据" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1qsurNgDqK1RjSZSyXXaxEVXa-300-90.png" height="60" alt="数云" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB13aywNhTpK1RjSZR0XXbEwXXa-98-38.png" height="60" alt="广通软件" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1xqmBNjTpK1RjSZKPXXa3UpXa-162-70.png" height="60" alt="菜菜" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB18DmINcfpK1RjSZFOXXa6nFXa-200-200.png" height="60" alt="科蓝公司" /></td>
</tr>
<tr>
<td align="center"><img src="https://img.alicdn.com/tfs/TB15uqANXzqK1RjSZFoXXbfcXXa-188-86.png" height="60" alt="浩鲸" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1mvmyNkvoK1RjSZPfXXXPKFXa-238-46.png" height="60" alt="未名天日语" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1PSWsNmrqK1RjSZK9XXXyypXa-195-130.jpg" height="60" alt="金联创" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1k1qzNbvpK1RjSZFqXXcXUVXa-160-69.png" height="60" alt="同窗链" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1UdaGNgHqK1RjSZJnXXbNLpXa-277-62.png" height="60" alt="百世快递" /></td>
</tr>
<tr>
<td align="center"><img src="https://img.alicdn.com/tfs/TB17OqENbrpK1RjSZTEXXcWAVXa-240-113.jpg" height="60" alt="汽车之家" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1q71ANkvoK1RjSZPfXXXPKFXa-257-104.png" height="60" alt="鲸打卡" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1UzuyNhTpK1RjSZR0XXbEwXXa-201-86.jpg" height="60" alt="时代光华" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB19RCANgHqK1RjSZFPXXcwapXa-180-180.jpg" height="60" alt="康美" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1iCGyNb2pK1RjSZFsXXaNlXXa-143-143.jpg" height="60" alt="环球易购" /></td>
</tr>
<tr>
<td align="center"><img src="https://avatars0.githubusercontent.com/u/16344119?s=200&v=4" height="60" alt="Nepxion" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1aUe5EpzqK1RjSZSgXXcpAVXa-248-124.png" height="60" alt="吃瓜" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1H9O5EAvoK1RjSZFNXXcxMVXa-221-221.jpg" height="60" alt="宅无限" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1rNq4EwHqK1RjSZFgXXa7JXXa-200-200.jpg" height="60" alt="天阙" /></td>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1CRAxDxYaK1RjSZFnXXa80pXa-190-190.jpg" height="60" alt="联合永道" /></td>
</tr>
<tr>
<td align="center"><img src="https://img.alicdn.com/tfs/TB1.q14ErrpK1RjSZTEXXcWAVXa-219-219.jpg" height="60" alt="明源云" /></td>
<td align="center"><img src="https://www.daocloud.io/static/Logo-Light.png" height="60" alt="DaoCloud" /></td>
<td align="center"><img src="https://www.meicai.cn/img/logo.9210b6eb.jpg" height="60" alt="美菜" /></td>
<td align="center"><img src="https://www.sunline.cn/u_file/fileUpload/2021-06/25/2021062586431.png" height="60" alt="长亮科技" /></td>
<td align="center"><img src="https://www.sinocare.com/sannuo/templates/web/img/bocweb-logo.svg" height="60" alt="三诺生物" /></td>
</tr>
</table>

**[View all users & add your company](https://github.com/alibaba/nacos/issues/273)**
