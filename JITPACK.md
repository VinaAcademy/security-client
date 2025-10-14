# Hướng dẫn Publish và Sử dụng với JitPack

## Giới thiệu

JitPack là một dịch vụ build Maven/Gradle repository từ GitHub. Nó cho phép bạn publish các thư viện Java/Kotlin trực tiếp từ GitHub repository mà không cần setup Maven Central hay private repository phức tạp.

## Cách hoạt động

1. Bạn push code lên GitHub và tạo một tag/release
2. JitPack tự động detect và build project khi có request đầu tiên
3. JitPack cache artifact để sử dụng cho các request tiếp theo

## Publish Library lên JitPack

### Bước 1: Đảm bảo project sẵn sàng

Kiểm tra `pom.xml` có đầy đủ thông tin:

```xml
<groupId>vn.vinaacademy</groupId>
<artifactId>security-client</artifactId>
<version>1.0.0</version>
```

### Bước 2: Commit và push code

```bash
git add .
git commit -m "Release v1.0.0"
git push origin master
```

### Bước 3: Tạo tag và push lên GitHub

```bash
# Tạo tag cho version mới
git tag v1.0.0

# Push tag lên GitHub
git push origin v1.0.0

# Hoặc push tất cả tags
git push --tags
```

### Bước 4: Kiểm tra build trên JitPack

Truy cập: https://jitpack.io/#VinaAcademy/security-client-library

- JitPack sẽ tự động detect tag mới
- Click vào "Get it" để trigger build
- Chờ build hoàn tất (có thể mất vài phút cho lần đầu)
- Build log sẽ hiển thị nếu có lỗi

### Bước 5: Xác nhận artifact đã available

Khi build thành công, bạn sẽ thấy badge màu xanh và có thể copy dependency snippet.

## Sử dụng Library từ JitPack

### Trong project khác

#### 1. Thêm JitPack repository

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

#### 2. Thêm dependency

```xml
<dependency>
  <groupId>com.github.VinaAcademy</groupId>
  <artifactId>security-client-library</artifactId>
  <version>v1.0.0</version>
</dependency>
```

**Lưu ý**: 
- GroupId là `com.github.VinaAcademy` (tên organization/user trên GitHub)
- ArtifactId là `security-client-library` (tên repository)
- Version là tag name (bao gồm cả prefix `v` nếu có)

#### 3. Build project

```bash
mvn clean install
```

Maven sẽ tự động download artifact từ JitPack.

## Version Management

### Sử dụng Semantic Versioning

Recommended format: `vMAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes (v2.0.0)
- **MINOR**: New features, backward compatible (v1.1.0)  
- **PATCH**: Bug fixes (v1.0.1)

### Các cách specify version

```xml
<!-- Specific version -->
<version>v1.0.0</version>

<!-- Branch name (latest commit) -->
<version>master-SNAPSHOT</version>

<!-- Commit hash -->
<version>abc123def</version>

<!-- Latest release -->
<version>LATEST</version>
```

**Best practice**: Sử dụng specific version tags cho production.

## Troubleshooting

### Build failed trên JitPack

1. Click vào "Look up" button trên JitPack.io
2. Xem build log để tìm lỗi
3. Các lỗi thường gặp:
   - Missing parent POM (đảm bảo parent POM cũng available)
   - Compilation errors
   - Missing dependencies

### Dependency resolution failed

```bash
# Clear Maven cache
mvn dependency:purge-local-repository

# Or manually delete cache
rm -rf ~/.m2/repository/com/github/VinaAcademy
```

### Force rebuild trên JitPack

Truy cập: https://jitpack.io/#VinaAcademy/security-client-library/v1.0.0

Click vào icon refresh để force rebuild.

## Advanced: Private Repository

Nếu repository là private, bạn cần:

1. Tạo JitPack account và liên kết GitHub
2. Thêm authentication token vào Maven settings.xml:

```xml
<servers>
  <server>
    <id>jitpack.io</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_JITPACK_AUTH_TOKEN</password>
  </server>
</servers>
```

## Parent POM và JitPack

Project này sử dụng `vinaacademy-parent:2.0.0` từ JitPack:

```xml
<parent>
  <groupId>com.github.VinaAcademy</groupId>
  <artifactId>vinaacademy-parent</artifactId>
  <version>2.0.0</version>
</parent>
```

Đảm bảo parent POM cũng đã được publish lên JitPack trước khi publish child modules.

## Best Practices

1. **Luôn tạo tag cho releases**: Dùng tags thay vì commit hash hoặc branch names
2. **Semantic versioning**: Tuân theo format vMAJOR.MINOR.PATCH
3. **Test trước khi release**: Build và test locally trước khi tạo tag
4. **Changelog**: Maintain CHANGELOG.md để track changes
5. **Documentation**: Update README.md với mỗi release
6. **Cache**: JitPack cache artifacts, có thể mất vài phút để invalidate

## Useful Links

- JitPack Homepage: https://jitpack.io
- Project on JitPack: https://jitpack.io/#VinaAcademy/security-client-library
- JitPack Docs: https://jitpack.io/docs/
- Build Logs: https://jitpack.io/com/github/VinaAcademy/security-client-library/

## Example Workflow

```bash
# 1. Make changes
git add .
git commit -m "feat: add eureka service discovery support"

# 2. Update version in pom.xml
mvn versions:set -DnewVersion=1.1.0

# 3. Test locally
mvn clean install

# 4. Push changes
git push origin master

# 5. Create and push tag
git tag v1.1.0
git push origin v1.1.0

# 6. Wait for JitPack build
# Visit https://jitpack.io/#VinaAcademy/security-client-library/v1.1.0

# 7. Use in other projects
# Update dependency version to v1.1.0
```
