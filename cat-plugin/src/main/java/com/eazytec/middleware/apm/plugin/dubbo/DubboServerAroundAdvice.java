package com.eazytec.middleware.apm.plugin.dubbo;

import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.dianping.cat.Cat;
import com.eazytec.middleware.apm.advice.CatAroundAdvice;

import java.lang.reflect.Method;

public class DubboServerAroundAdvice extends CatAroundAdvice {

    @Override
    protected String getTransactionType(String originMethodName, Object obj, Method method, Object[] args) {
        return "Service";
    }

    @Override
    protected String getTransactionName(String originMethodName, Object obj, Method method, Object[] args) {
        if(args.length > 0 && args[0] != null){
            try{
                RpcInvocation rpcInvocation =  (RpcInvocation)args[0];
                return "[dubbo] "+rpcInvocation.getInvoker().getInterface().getSimpleName()+"."+rpcInvocation.getMethodName();
            }catch (Exception ignore){  }
        }
        return "[dubbo] unknown";
    }

    @Override
    protected void doBefore(String originMethodName, Object obj, Method method, Object[] args){
        if(args.length > 0 && args[0] != null) {
            RpcInvocation rpcInvocation = (RpcInvocation) args[0];

            RemoteContext context = new RemoteContext();
            context.addProperty(Cat.Context.CHILD, rpcInvocation.getAttachment(D_TRACE_CHILD_ID));
            context.addProperty(Cat.Context.PARENT, rpcInvocation.getAttachment(D_TRACE_PARENT_ID));
            context.addProperty(Cat.Context.ROOT, rpcInvocation.getAttachment(D_TRACE_ROOT_ID));
            Cat.logRemoteCallServer(context);

            Cat.logEvent(E_CLIENT_ADDR,rpcInvocation.getAttachment(D_CLIENT_ADDR));
            Cat.logEvent(E_CLIENT_DOMAIN,rpcInvocation.getAttachment(D_CLIENT_DOMAIN));
        }
    }
    @Override
    protected void doAfter(String originMethodName, Object obj, Method method, Object[] args,Object res){
        RpcResult result = (RpcResult)res;
        result.setAttachment(D_CALL_SERVER_DOMAIN, Cat.getManager().getDomain());
        result.setAttachment(D_CALL_SERVER_ADDR, Cat.getManager().getThreadLocalMessageTree().getIpAddress());
    }
}
