public class File_Load {
    public static void main(String[] args) {
        /*
        文件上传
         */
        //前端如果使用表单来上传文件，有三点需要注意：
        //
        //form标签必须把enctype属性设置为：multipart/form-data
        //form标签的method属性必须设置为：post
        //通过在input标签中把type属性设置为file，创建文件上传框
        //常用例子：
        @ResponseBody
        @PostMapping("/demo/save/file/upload")
        public String saveFileUpload(@RequestPart("picture") MultipartFile file) throws IOException {
            String inputName = file.getName();
            System.out.println("inputName = " + inputName);

            String originalFilename = file.getOriginalFilename();
            System.out.println("originalFilename = " + originalFilename);

            long size = file.getSize();
            System.out.println("size = " + size);

            String contentType = file.getContentType();
            System.out.println("contentType = " + contentType);

            byte[] bytes = file.getBytes();
            System.out.println("bytes = " + bytes);

            InputStream inputStream = file.getInputStream();
            System.out.println("inputStream = " + inputStream);
            return "ok";
        }

        //多文件上传(同名)
        @RequestMapping("/upload/multi/file/same/name")
        public String uploadMultiFileSameName(@RequestPart("picture") List<MultipartFile> pictureList) {

            for (MultipartFile multipartFile : pictureList) {
                if (!multipartFile.isEmpty()) {
                    String originalFilename = multipartFile.getOriginalFilename();
                    System.out.println("originalFilename = " + originalFilename);
                }
            }

            return "ok";
        }
        //多文件上传(不同名)
        @RequestMapping("/upload/multi/file/different/name")
        public String uploadMultiFileDifferentName(
                @RequestPart("picture") MultipartFile picture,
                @RequestPart("image") MultipartFile image) {

            String originalFilename = picture.getOriginalFilename();
            System.out.println("originalFilename = " + originalFilename);

            originalFilename = image.getOriginalFilename();
            System.out.println("originalFilename = " + originalFilename);

            return "ok";
        }

        /*
          文件下载
         */
        //通过设置响应消息头：
        //
        //告诉浏览器，当前请求返回的响应，是一个附件（不是一个页面）
        //告诉浏览器，当前响应体的内容类型
        //告诉浏览器，当前响应体对应的文件名
        //其中，Content-Type和Content-Disposition是实现文件下载的核心消息头，其他消息头用于优化下载体验和兼容性

        //响应数据整体封装类
        //父类：org.springframework.http.HttpEntity
        //子类：org.springframework.http.ResponseEntity

        //响应消息头封装类
        //接口：org.springframework.util.MultiValueMap
        //实现类：org.springframework.http.HttpHeaders（有无参构造器）

        //放在类路径下，为了不管在本地运行还是部署到服务器，都能够通过相同的代码读取到文件

        //代码实现：
        @ResponseBody
        @GetMapping("/demo/download/file")
        public ResponseEntity<byte[]> downloadFile() throws IOException {

            // 1、把要下载的目标文件，读取到内存中
            String fileName = "god.jpg";
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);

            byte[] bytes = inputStream.readAllBytes();

            // 2、创建响应消息头
            HttpHeaders httpHeaders = new HttpHeaders();

            // [1]Content-Disposition：设置为attachment; filename=xxx强制浏览器下载文件
            httpHeaders.setContentDispositionFormData("attachment", fileName);

            // [2]Content-Type：设为application/octet-stream表示二进制流
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            // [3]Content-Length：设置文件大小，可优化下载进度显示
            httpHeaders.setContentLength(bytes.length);

            // 3、创建 ResponseEntity 对象并返回
            return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
        }

        //改进：支持中文文件名
        //需要针对中文文件名进行编码
        //代码实现：
        // 读取文件时，仍然使用本来的文件名
        String fileName = "金刚战神.jpg";
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);

        byte[] bytes = inputStream.readAllBytes();

        HttpHeaders httpHeaders = new HttpHeaders();

        // 在设置响应消息头时才进行编码
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);//核心语句
        httpHeaders.setContentDispositionFormData("attachment", fileName);
        }


        //改进：大文件下载优化
        //原理：每次接收并写入一部分数据，而不是一次接收所有数据
        //代码实现：
        @ResponseBody
        @GetMapping("/demo/download/large/file")
        public ResponseEntity<StreamingResponseBody> downloadLargeFile() {
            // 示例：流式下载大文件
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=largeVideo.mp4")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(outputStream -> {
                        // 从输入流复制到输出流（如：文件、网络流等）
                        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("largeVideo.mp4")) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    });
        }




















}