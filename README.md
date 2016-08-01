# coke
基于BDF的辅助工具包---forked from cnxobo/coke
##分支更新记录：
###0.0.10更新
* 支持获取部门key-value转换，使用方法，在DataType上需要转换的字段mapping属性mapValues设置为${coke.dict.lookup("coke.depts",arg1)}，参数arg1代表部门表的实体名称，空值即为bdf默认的DefaultDept。

###0.0.8更新
* 支持Entity自定义ID规则，Entity需要同时实现IBase和IBaseGeneratAble接口。Id生成规则在IBaseGeneratAble的GenerateID方法中实现。通过HibernateSupportDao的insertEntity或persistEntities方法进行数据库插入操作时会自动生成ID。
* 修改HibernateSupportDao中泛型定义。
