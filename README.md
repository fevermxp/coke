# coke
基于BDF的辅助工具包---forked from cnxobo/coke
##分支更新记录：
###0.0.8更新
* 支持Entity自定义ID规则，Entity需要同时实现IBase和IBaseGeneratAble接口。Id生成规则在IBaseGeneratAble的GenerateID方法中实现。通过HibernateSupportDao的insertEntity或persistEntities方法进行数据库插入操作时会自动生成ID。
