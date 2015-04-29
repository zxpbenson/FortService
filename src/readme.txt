rm -r /usr/local/fort_append/FortDecryption

rm /bin/FortDecryption

上传catch_pwd.jar,FortService.jar,FortService三个文件到/root/目录下

mkdir -p /usr/local/fort_append/FortService

mv catch_pwd.jar /usr/local/fort_append/FortService/catch_pwd.jar

mv FortService.jar /usr/local/fort_append/FortService/FortService.jar

mv FortService /bin/FortService

chmod +x /bin/FortService

test:

FortService Asset Asset_002A5D869 root

FortService Asset Asset_002A5D869 root true

FortService Person zhangke 123

FortService Person zhangke 123 true

FortService Authorization zhangke Asset_1351712111964296 support

FortService Authorization zhangke Asset_1351712111964296 support true

FortService Authorization get zhangke 13165170732686 - -

FortService Authorization get zhangke 13165170732686 true - -

FortService Role zhangke Asset_0031B20A8

FortService Role zhangke Asset_0031B20A8 true
