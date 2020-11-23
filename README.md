[toc]
# 前言
最近一年多在公司面试很多资深工程师的岗位，当聊起微服务架构的时间，有一个不可避免的话题-服务之间的调用。很多面试者都会说服务之间是rpc调用，基本能说出dubbo,feignclient调用方式。 然而当问起rpc是怎么实现的，到目前为止还没有面试者能讲清楚的，很多小伙伴都只能说出怎么调用，然而却无法从底层实现上讲出来的，其中包括很多工作10年的面试者。一个不了解底层实现原理的人，是无法适应架构师的岗位的。本人秉持一个原则，知其然，知其所以然，写此文，望助各位奋战在JAVA架构之路上的小伙伴一臂之力。

# 目标
万物之始,大道至简,衍化至繁-老子。本文使用最简单的方式，最少依赖来讲述并实现rpc的调用，使Java小伙伴能彻底理解底层实现原理跟实现方式

# 技术点
-  springboot 
-  fastjson, 
-  java反射， 
-  okhttp, 
-  springMVC(Ioc)
# 源码
  各位小伙伴可以看本文的同时，把源码[下载](https://github.com/wesley-zhong/rpcstudy.git)下来，对着源码看本文，更好理解。
  
# rpc调用方式
## 什么是rpc
   rpc remote process call , 远程过程调用
## 怎么调用
###  定义一个接口
#### 定义入参类
```
@Data
public class UserLoginInfo {
    private String userName;
    private String password;
}
```
#### 定义方法
```
@Rpc
public interface UserLoginService extends RpcController {
     UserInfo userLogin(UserLoginInfo userLoginInfo);
}
```

### 在server端实现
   
```
@Component
@Slf4j
public class UserLoginServiceImpl implements UserLoginService {
    public UserInfo userLogin(UserLoginInfo userLoginInfo) {
        log.info("receive from client username ={} password={}", userLoginInfo.getUserName(), userLoginInfo.getPassword());
        UserInfo  userInfo = new UserInfo();
        userInfo.setUserNickName("nick_" + userLoginInfo.getUserName());
        userInfo.setUserId(100);
        return userInfo;
    }
}

```
客户端调用后日志输出：

```
 c.s.r.service.impl.UserLoginServiceImpl  : receive from client username =wesley password=abc
```


### 客户端调用
 我们期望通过实例化 UserLoginService 实例 userLoginService实例通过  ==userLoginService.userLogin==() 方法来调用 server端的  ==userLogin== 方法
```
    @Autowired
    private UserLoginService userLoginService
    
    String  userName = "wesley";
    UserLoginInfo userLoginInfo = new UserLoginInfo();
    userLoginInfo.setUserName(userName);
    userLoginInfo.setPassword(password);
    //调用上面服务端的实现
    UserInfo  userInfo = userLoginService.userLogin(userLoginInfo);
    log.info("########get from server  userNickName = {}",userInfo.getUserNickName());
```
此时 日志输出：

```
c.s.r.s.UserServiceImpl.UserServiceImpl  : ########get from server  userNickName = nick_wesley
```

   
# 实现方法
一个进程需要调用另一个进程的方法，有哪些手段

```
sequenceDiagram
client->>server: call serverMethod
server->>client: return    result 
```

## 进程间通信方式
  1. 管道( pipe )
  2. 信号量( semophore )
  3. 消息队列( message queue ) 
  4. 信号 ( sinal )
  5. 共享内存( shared memory ) 
  6. 套接字( socket ) 
  

## 如何选择
   前面5中通信方式，都是同一机器上实现的，借助os来实现。只有==套接字( socket )== 才能实现不同机器上进程间的通信
   
## 协议定制
### 客户端（调用方）
需要告诉服务器（被调用方），调用服务器的那个类的那个方法
### 服务端（被调用方）
通过解析客户端的请求，来调用对应的方法
### 通信协议
#### 统一请求 RpcRequest

```
public class RpcRequest {
    private String serviceName;
    private String methodName;
    private String payLoad;      //body
}

```
备注： 这只是其中的一种比较直观的定义方式，还可以N种变种，例如可以定义如下：
```
{
    private long msgId;         // 消息ID 
    private String payLoad;     // 调用的参数数据
}
```
不过对应不同的协议，client 跟server 端的实现都是不一样，但原理不变
#### 统一响应 RpcResponse

```
public class RpcResponse {
    private int errorCode;   //错误码
    private String errorMsg;//错误信息
    private Object data;  //rpc返回结果对象
}

```
#### 调用请求实例
按照我们以上定义的协议，如果我们我们想要调用服务端的  ==UserLoginService== 下的方法  ==userLogin==，那么我们应该这样来实例化 RpcRequest

```
   RpcRequest rpcRequest = new RpcRequest();
   rpcRequest.setMethodName("UserLoginService");
   rpcRequest.setMethodName("userLogin");

   //set parameter
   UserLoginInfo   userLoginInfo = new UserLoginInfo();
   userLoginInfo.setPassword("abc");
   userLoginInfo.setUserName("wesley");
   rpcRequest.setPayLoad(JSON.toJSONString(userLoginInfo));
```

包的请求内(json)如下

```
{
	"methodName": "userLogin",
	"serviceName": "UserLoginService",
	"payLoad": "{\"password\":\"abc\",\"userName\":\"wesley\"}"
}
```


# server 端实现
## 定义springMVC拦截器
```
    @RequestMapping(value = "/", method = {RequestMethod.POST})
    @ResponseBody
    public RpcResponse callRpc(@RequestBody RpcRequest rpcRequest) {
    }
```
拦击所有请求，解析统一请求包==RpcRequest==

## 解析协议，调用具体实现类，方法
 此时==RpcRequest==  中的 serviceName 值为 ***UserLoginService***， methodName值为 ***userLogin***.我们需要找到 **UserLoginService**实例中的
 名字为**userLogin**的方法

## 如何调用
### SpringIoc
管理所有需要RPC调用的service

#### 定义RpcController

```
public interface RpcController {
}
```
定义空的接口类，SpringIoc 特性会把实现这个接口的类实例到同一个集合中，如我们有一个***UserLoginService1*** 和  ***UserLoginService2***   如下：
```

public interface UserLoginService1 extends RpcController ；

public interface UserLoginService2 extends RpcController ；
```
#### 管理RPC service 集合

```
    @Autowired
    private List<RpcController> rpcControllerList;  //UserLoginService1,UserLoginService2  实例集合
```
此时我们只要这也声明，SpringIoc 会把**UserLoginService1*** 和  ***UserLoginService2***  全部注入到
#### 获取RPC service 实例

```
    private RpcController getServiceByName(String serviceName) {
        for (RpcController rpcController : rpcControllerList) {
            Class<?>[]  interfaces = rpcController.getClass().getInterfaces();
            if(interfaces == null || interfaces.length == 0){
                continue;
            }
            for(Class  interClass : interfaces){
                if(interClass.getSimpleName().equals(serviceName)){
                    return rpcController;
                }
            }
        }
        return null;
    }
```


#### 获取RPC service中的Method实例

```
    private Method getServiceMethod(RpcController rpcController, String methodName) {
        Method[] methods = rpcController.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
```
通过反射机制获取service 中  Method 实例

### 反序列方法中的参数
通过上面获取Method 实例后，我们可以获取Method 对象中的parameter 的个数以及类型
```
   //获取method的入参类型
    Type[] types = method.getGenericParameterTypes();
    Object parameter = null;
    //only process 0 or 1 args  others not process
    if (types != null && types.length == 1) {
        //json 反序序列参数实例
        parameter = JSON.parseObject(rpcRequest.getPayLoad(), types[0]);
    }
```
## RPC方法调用
完事已具备，rpc调用如下：

```
    public Object callServiceMethod(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getServiceName();
        String methodName = rpcRequest.getMethodName();

        //获取RPC service 实例
        RpcController rpcController = getServiceByName(serviceName);
        if (rpcController == null) {
            throw new LogicException(1, "service not exist");
        }
        //获取RPC service 实例中的方法
        Method method = getServiceMethod(rpcController, methodName);
        if (method == null) {
            throw new LogicException(2, "method not exist");
        }
        //获取method的入参类型
        Type[] types = method.getGenericParameterTypes();
        Object parameter = null;
        //only process 0 or 1 args  others not process
        if (types != null && types.length == 1) {
            //json 反序序列参数实例
            parameter = JSON.parseObject(rpcRequest.getPayLoad(), types[0]);
        }
        try {
            return method.invoke(rpcController, parameter);
        } catch (Exception e) {
            throw new LogicException(3, e.getMessage());
        }
    }
```
## 统一入口
```
    @RequestMapping(value = "/", method = {RequestMethod.POST})
    @ResponseBody
    public RpcResponse callRpc(@RequestBody RpcRequest rpcRequest) {
        RpcResponse rpcResponse = new RpcResponse();
        try {
            Object ret = serviceMgr.callServiceMethod(rpcRequest);
            rpcResponse.setData(ret);
        } catch (LogicException e) {//统一异常处理
            rpcResponse.setErrorCode(e.getErrorCode());
            rpcResponse.setErrorMsg(e.getMessage());
            e.printStackTrace();
        } catch (Throwable e) { //统一异常处理
            rpcResponse.setErrorCode(-1);//unknown exception
            rpcResponse.setErrorMsg(e.getMessage());
            e.printStackTrace();
        }
        return rpcResponse;
    }
```
统一入口处，==做统一返回==，==统一异常处理==，结果放到***RpcResponse***对象中返回给客户端

## PostMan 测试
由于图片不好插入，这里给出输入，输出
### URL
POST http://localhost:8080

### body：

```
{
	"methodName": "userLogin",
	"serviceName": "UserLoginService",
	"payLoad": "{\"password\":\"abc\",\"userName\":\"wesley\"}"
}
```

### response:

```
{
    "errorCode": 0,
    "errorMsg": null,
    "data": {
        "userId": 100,
        "userNickName": "nick_wesley"
    }
}
```

### server 端日志输出：
[nio-8080-exec-4] c.s.r.service.impl.UserLoginServiceImpl  : receive from client username =wesley password=abc


在处理rpc 请求时，做所有异常捕捉，对应的错误信息赋值***errorMsg***


# client 端实现
## 实现思路
有了上面我们在server的端rpc定义和实现，那么client端的实现思路就有了，
对于client 端的调用
```
UserLoginService.userLogin(UserInfo  userInfo)
```
我们只要能转化成 body 为：

```
{
	"methodName": "userLogin",
	"serviceName": "UserLoginService",
	"payLoad": "{\"password\":\"abc\",\"userName\":\"wesley\"}"
}
```
的http 请求，并发送到server端就能调用到server端对应的方法，这样我们就实现了rpc的调用。
要完成这个目标我们需要完成以下几件事

- 实例化***UserLoginService***
- 获取 ***serviceName***属性值
- 获取 ***methodName***属性值
- 截取 ***userLogin***方法的调用，并转化我们想要发送的请求给server端


##  UserLoginService 实例化
我们先看 UserLoginService 的声明

```
@Rpc
public interface UserLoginService extends RpcController {
     UserInfo userLogin(UserLoginInfo userLoginInfo);
}

```
再看一下，客户端调用方式

```
   //注入rpc client 实例
    @Autowired
    private UserLoginService userLoginService;
    
    //请求参数实例化
    UserLoginInfo userLoginInfo = new UserLoginInfo();
    userLoginInfo.setUserName("wesley");
    userLoginInfo.setPassword("abc");
    
    
    //rpc 调用
    UserInfo userInfo = userLoginService.userLogin(userLoginInfo);//服务端放回 UserInfo 对象
```


有经验的同学，一眼就看出了，在客户端我们并没有实现UserLoginService的接口，并做@annotation 声明，springIoc也不会，给我们实例化，这样在程序启动的时间就会报错的。那么我们该怎么做，才能在程序启动时间加载好 *** UserLoginService*** 实例呢？

### SpringBoot启动加载实例
SpringBoot 的启动加载过程是很复杂的，这不是本文的重点，本文不做详细阐述，只给读者一个实现的方式
#### @EnableRpcClient
大家在使用SpringBoot 时经常会在在Appplication的启动类中看到类似的注解如：

```
@SpringBootApplication
@EnableAutoConfiguration
@EnableEurekaClient
...
```
等等，这些注解是告诉SpringBoot在启动的时间，要做做些什么事情，在注解中，我们可以做一些事情，例如 ==@EnableRpcClient== 是这样定义的

```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({RpcClientRegister.class})
public @interface EnableRpcClient {
    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<?>[] defaultConfiguration() default {};

    Class<?>[] clients() default {};
}

```
这其中最关键的点 ==@Import({RpcClientRegister.class})==，当Springboot启动时，会实例化 类 ***RpcClientRegister***，虽然看似有点复杂,这是springIoc 的一种实现方式，功能，会扫描指定的包下的带有@Rpc 注解的的类并结合 ***FactoryBean***来实例化，具体实现在源码中.

#### @Rpc
我们在定义接口的时间有 ==@Rpc==  这么个annotation,这个是我们自己定义的注解，在springBoot启动过程通过 ***@EnableRpcClient***注解中的 ***RpcClientRegister***类 我们需要扫描那些JAVA上有这个注解，我们需要时采用SpringIoc 的方式来注入到SpringBeanFactory 中，这样实例化就完成了。
***SpringFactorybean***最重要的方法

```
    @Override
    public Object getObject() {
        return getTarget(); //在下面讲解如何实现
    }
    
   public Class<?> getObjectType() {
        return this.type;
    }
 
  // 这里入参 type 是 UserLoginService.class
    public void setType(Class<?> type) {
        this.type = type;
    }
```




##  SerivceName 获取
   通过***SpringFactorybean*** 中的 ***type*** 属性，***type.getSimpleName()***即可得到

## methodName 获取
在方法调用的时间获取
## 代理***userLogin***方法的调用
springIoc 的实现都是基于动态代理的，我们要获取一个类的所有方法调用，都可以采用动态代理,不了解的同学去搜一下J==AVA动态代理==的相关资料

```
    Object getTarget() {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args){
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setMethodName(method.getName());
                rpcRequest.setServiceName(type.getSimpleName());
                rpcRequest.setPayLoad(JSON.toJSONString(args[0]));
                String responseBody = HttpUtils.okhttpReq(rpcRequest);
                log.info("-----------  call service={} method ={}  args ={}  response ={}",
                        type.getSimpleName(), method.getName(), JSON.toJSONString(args), responseBody);

                Class<?> returnType = method.getReturnType();
                RpcResponse response = JSON.parseObject(responseBody, RpcResponse.class);
                if (response.getErrorCode() != 0) {
                    throw new LogicException(response.getErrorCode(), response.getErrorMsg());
                }
                return JSON.parseObject(JSON.toJSONString(response.getData()), returnType);
            }
        };
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

```
这里我们实现了带有==Rpc==注解的动态代理，当任何接口类中带有 ==Rpc== 注解的，我们都把其中的方法都代理成了上面的实现，比如

```
UserInfo userInfo = userLoginService.userLogin(userLoginInfo);
```
那么 ***public Object invoke(Object proxy, Method method, Object[] args)***方法中

- proxy : userLoginService 实例
- method ： userLogin 
- args ： userLoginInfo

这里做简单阐述一下，源码中可以跑起来调试，日志写的也比较详细。
# 源码运行指南
[源码地址](https://github.com/wesley-zhong/rpcstudy.git)

## 模块目录
- rpc-interface   公共接口rpc接口，client  server 端公用的类
- rpc-server    server 端的实现
- rpc-cleint   client 端的实现

## 运行
1. cd  rpc-interface 
2. mvn clean
3. mvn install
依次执行rpc-server   rpc-cleint  

## idea 调式
open rpc-cleint 
依次到各个模块，右键 ->add as maven project

# 结语
本文屏蔽了很多不必要的细节，力求使用最简单的方式，给读者介绍并实现了rpc的实现方式，与各位小伙伴共勉。本文主要是介绍方法实现，供学习使用。但不能直接使用到正式的开发环境，这里使用了最原始的反射，有些许性能问题，需要优化，另外正式环境需要负载均衡，熔断，限流，这些本文没做讲解。但这些的基础都是本文讲的知识。后续如果有需要，本人将从微服务架构，以及上面提到的这些方面入手，从现有的基础上优化，达到生产环境的要求。另外如果有任何问题，请联系 wiqi.zhong@gmail.com ,谢谢！。










