# JXPi
JXPi平台是设计用于中小规模的智能控制系统。传统的控制系统是集中性控制，但随着硬件的性能越来越强、越来越廉价，未来的控制体系将呈现为**微智能、协商规划、宏观控制**，基于这样的认识，JXPi是作为现场微系统的智能平台来开发的。

其设计目标是实现一个稳定可靠、可动态配置、弹性扩充的智能控制系统。为实现这一目标，JXPi平台采用了lua+java+python三者相融合的机制：

1、lua脚本用于实际控制功能的编写，系统用其实现动态配置、弹性扩充。笔者在开发一个控制系统时，需要在开发机上写好程序、编译、然后上传到连接了各种器件的测试机、停止之前的系统，运行新程序进行测试。有时一个小时内就需重复多次，效率太过低下。在后期需增加功能时也要将正在运行中的生产机停下来，造成系统失效，在应用系统中这可能还可以接受，但对于现场控制系统来说可能就无法接受了。所以就产生了将稳定可靠的平台和各种控制功能相分离的想法，最终实现为现在的JXPi平台。进一步的，JXPi平台还引入了配置项版本的概念，系统会定时检查各项配置，如果其版本号增加了，则系统会自动将之前的配置清除并导入新的配置，而这一过程一是自动的，二是隔离的，即如果新的配置出了问题也不会影响到其它部分的正常工作，由此，JXPi平台就具有了运行中切换与升级的能力。

2、java用于提供一个稳定而可靠的系统平台，并提供了丰富的基础设施。JXPi平台的主体是用java开发，提供了基本的web服务、数据库（sqlite，也支持其它数据库）访问、网络通信、外部设备访问接口、各部件勾连通道等。系统平台的首要目标是为控制系统提供一个稳定而可靠的基础平台，由于已将繁杂的控制功能从系统平台中移出，所以该平台是非常坚固的。同时，系统还提供了一些非常有用的功能，如状态机、异步操作、json、ORM等等。系统平台主要提供了如下的功能：

- 外部访问接口：系统以REST方式提供了可供web或其它外部设备访问的系统接口，系统已构建了完整的REST接口框架，可以快速简便的按需提供REST接口

- 各组成部分的勾连：系统打通了系统和lua、python、前端之间的沟通通道，使得各部分能够有机的融合为一体，用于控制的lua脚本在系统平台以及后端python的支持下终将无所不能

- 强大的扩展能力：JXPi平台提供了简便的机制可通过lua/python进行定制性的扩展，详见附录四中的说明

- 前端设备的勾连：在一个互联的世界里，除了需要被其他人或系统访问，也需要访问其它的设备或系统，系统将对外部设备的访问封装为lua模块，使用者可以直接在lua脚本中实现对前端设备的访问和控制

- 通常的系统管理：日志、访问控制、数据库等等

3、python用于在后台充分利用python所积累的丰富资源来扩充系统的功能。利用JXPi平台提供的非常简便的扩展机制，可方便的将python代码集成到JXPi平台中来。

简单的讲，JXPi平台是希望提供一个稳定、可靠、坚固的控制系统平台，然后在这个基础之上提供了各种简便的扩展接口，可以简便的将各种资源加以快速而低成本的黏合。

JXPi平台同时提供了lua接口和REST接口，摆脱前端界面，JXPi平台可以作为哑终端完全通过lua脚本检测输入信号来实现普通的控制系统的工作。而利用REST接口，则可通过web、手机、其它智能系统等前端实现对其进行可视化操作。作为一个整体，JXPi平台在发布时，提供了一个web的访问前端，在系统运行后，可直接通过web进行访问。

JXPi平台的使用：直接运行命令即可:

sudo java -jar jxpi.jar &

## 版本说明

**v0.7.1(2016.7.25)**

- 增加了文件下载功能。

- 对参考手册进行了修改。

**v0.7.0(2016.7.16)**

- 调整权限设置的功能，并增加相应的文档说明。

- 增加lua接口readConf，并补充了lua脚本的使用说明。

- 修复虚拟设备的bug，增强了虚拟设备接口的能力，提供客户端的读写与日志功能、服务端的注册事件登记、输入事件登记、端口状态设置等功能。

**v0.6.0(2016.7.9)**

- 增加虚拟设备访问接口。虚拟设备是通过TCP可访问JXPi平台的软件系统，目前仅有java接口。这一虚拟设备的引入主要是为未来更广泛的JXPi平台分布式应用提供支持。即，未来多个JXPi系统、安卓手机或其它多种前端与设备等可组成一个智能控制系统。

- 为教学辅助系统的代码增加注释；修改stub相关的代码，并增加虚拟设备访问接口的源码文件。

- 在jxLua模块中开放流水号的功能。用户可在lua脚本中直接生成自己需要的流水号。

**v0.5.0(2016.7.2)**

- 对权限的实现进行了调整，主要考虑了如何兼容根据人员身份（如学生、老师等）进行权限管控和根据角色进行权限管控，也即是将JXPi平台的权限管控划分为两种方式：根据用户身份的预定义方式以及根据用户角色的动态检测。而动态检测部分的实现充分利用了lua前端设备操控机制，为其提供了lua脚本接口，可利用front中的虚拟设备的方式在lua脚本中灵活的对用户操作权限进行配置

	**注1**：JXPi平台原本只支持基于角色的权限管控，但对于小型应用系统来说，用角色进行权限管控还是过于复杂了。所以，JXPi平台又增加了基于身份进行权限管控的简便方案，但之前并未对这两种权限管控方式进行明确的区分，所以在应用上会容易混淆。所以在这一版本中进行了明确的切分，并将基于角色进行的权限管控移入lua脚本中实现

- 增加了一个“教学辅助系统”的示例。JXPi平台源自笔者多年来所积累的开发总结，最早可追溯到08年用.Net开发的AI预测平台和面向中小企业管理的云计算平台，在14年笔者将其核心功能用java进行了重写、调整与优化，在15、16年间又逐渐加入了智能控制的能力。所以，JXPi平台本质上是一个基础性的系统管理平台而不单单只是一个智能控制的系统。因此，新增了一个用JXPi平台开发的“教学辅助系统”的示例。需要注意的是，该示例仅用于说明如何利用JXPi平台快速而低成本的开发一个小型的应用系统，所以其功能是不完善的，这种不完善属于业务层面的不完善

- 增加了三个系统配置选项，主要用于未来对分布式控制系统的支持

**v0.4.0(2016.6.25)**

- 修复了ORM对象针对json属性字段的操作bug

- 扩展用户访问权限控制的自定义用户类型功能，使得用户可以更灵活的通过自定义用户类型来进行访问权限的控制

- 修复了json的转换函数中的bug

- 增加了ORM对象在转换为json时可控制某属性是否进行转换的功能

	**注1**：目前发现本系统的json转换似乎存在操作数量上的限制，具体原因尚未查清，为安全起见，最好将json字符串限制在750字符以下

	**注2**：由于数据库结构也发生些变化，所以需要将目录下的main.db数据库删除。系统在重新启动后会自动重新建立数据库，由于目前的平台功能未使用数据库进行数据存取，所以删除数据库并不影响系统功能

**v0.3.0(2016.6.18)**

- 修复了LRU的bug，该LRU用于缓存通过GetByID读取的ORM对象，从而降低数据库的select操作。

**v0.2.0(2016.6.11)**

- 修复了数据库多表读取时的重大bug，此bug为系统bug，多表联合查询时如有重名的列会导致数据错误。

- 增加了数据库增量添加ORM数据表的功能。本功能将使得根据需要在用户开发自己的升级版本时，当需要增加数据表时，不必手动建表。

	**注1**：本功能目前只能对新增的ORM数据表进行自动创建，对已建表的数据列的修改则无法实现自动修改

    **注2**：本功能在本版本的升级时会导致创建的所有数据表全部被删除重建！！严重警告：如果已在旧版本下增加了数据，需在升级前将main.db进行备份，等升级后，将原main.db复制回来。由于本手册并未使用到数据的存取，因此，可忽略该警告。根据本版本升级后的系统将不会再出现该问题


