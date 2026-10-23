THƯ MỤC CHỨA FILE THƯ VIỆN OBJECTDB (objectdb.jar)
=====================================================

1. Sau khi tải được file `objectdb.jar` (hoặc giải nén từ `objectdb-2.9.5.zip` trong thư mục bin/), hãy copy file `objectdb.jar` vào chính thư mục này:
   d:\CSDL_NC\co_so_du_lieu_nang_cao\db-normalization-tool\libs\objectdb.jar

2. Mở file `pom.xml` của dự án, bỏ dấu chú thích (uncomment) đoạn:
   <dependency>
       <groupId>com.objectdb</groupId>
       <artifactId>objectdb</artifactId>
       <version>2.9.5</version>
       <scope>system</scope>
       <systemPath>${project.basedir}/libs/objectdb.jar</systemPath>
   </dependency>

3. Chạy file `Main.java` trong package `com.doan10.db_normalization_tool.daotao` để kết nối và tạo file cơ sở dữ liệu `db/daotao.odb`.
