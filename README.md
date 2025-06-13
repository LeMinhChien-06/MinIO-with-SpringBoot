# MinIO-with-SpringBoot

### 1. Clone the project

```bash
git clone https://github.com/LeMinhChien-06/MinIO-with-SpringBoot.git
cd project-name
```

### 2. Sử dụng docker

```dockerfile
    docker run -p 9000:9000 -p 9001:9001 --name minio-server -e "MINIO_ROOT_USER=admin1" -e "MINIO_ROOT_PASSWORD=admin123" -v /path/to/data:/data minio/minio server /data --console-address ":9001"
```