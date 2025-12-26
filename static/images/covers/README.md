# 书籍封面图片目录

## 使用方法

1. **放置图片文件**
   - 将书籍封面图片文件（JPG、PNG等格式）放入此目录
   - 建议文件名使用书号，例如：`B001.jpg`、`B002.png`

2. **在管理员界面设置封面URL**
   - 添加或编辑书籍时，在"封面URL"字段输入：
     ```
     /images/covers/图片文件名.jpg
     ```
   - 例如：`/images/covers/B001.jpg`

3. **访问图片**
   - 图片将通过以下URL访问：
     ```
     http://localhost:8080/images/covers/图片文件名.jpg
     ```

## 注意事项

- 图片文件名建议使用书号，便于管理
- 支持的图片格式：JPG、PNG、GIF、WebP等
- 建议图片大小不超过2MB
- 建议图片尺寸：300x400像素或按比例缩放

## 示例

如果有一张名为 `B001.jpg` 的图片：
- 文件路径：`static/images/covers/B001.jpg`
- 在数据库中的URL：`/images/covers/B001.jpg`
- 访问URL：`http://localhost:8080/images/covers/B001.jpg`

