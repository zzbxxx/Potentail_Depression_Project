## 简介 | About

本项目专为**潜在抑郁症人群**设计。  
This project is designed for people with potential depression.

## 技术栈 | Tech Stack

| 模块 | Module             | 技术 | Technology         |
|------|--------------------|------|--------------------|
| 后端 | Backend            | Spring Boot | Spring Boot        |
| 前端 | Frontend           | Vue 3 | Vue 3              |
| 前端组件库 | UI Library       | Element Plus | Element Plus       |
| 状态管理 | State Management  | Pinia | Pinia              |
| 数据库 | Database          | MySQL + Redis | MySQL + Redis      |
| 文件上传服务 | File Storage     | AWS Backblaze | AWS Backblaze      |
| 部署方式 | Deployment        | Docker容器（Ubuntu）| Docker containers (Ubuntu) |

## 补充说明 | Additional Notes

- 前端使用 Element Plus 作为组件库。  
  The frontend uses Element Plus as the UI component library.

- 前端状态管理使用 Pinia。  
  The frontend uses Pinia for state management.

- 文件上传功能目前使用 AWS 的 Backblaze 服务。  
  File uploads are currently handled using AWS Backblaze service.

- MySQL 和 Redis 数据库在 Ubuntu 系统中通过 Docker 容器运行，需要自行进行相关配置。  
  MySQL and Redis databases run in Docker containers on Ubuntu system, relevant configurations need to be set up manually.