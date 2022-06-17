package com.bear.vocation.netty.common;

public class Constants {
    public static CommonResWrapper.CommonRes LOGIN_SUCCESS = CommonResWrapper.CommonRes.newBuilder()
            .setDataType(CommonResWrapper.CommonRes.DataType.StatusType)
            .setStatus(CommonResWrapper.CommonRes.Status.newBuilder()
                    .setCode(0)
                    .setMsg("")
                    .setStatusType(CommonResWrapper.CommonRes.Status.ReqTp.Login)
                    .build())
            .build();
    public static CommonResWrapper.CommonRes SENDMSG_SUCCESS = CommonResWrapper.CommonRes.newBuilder()
            .setDataType(CommonResWrapper.CommonRes.DataType.StatusType)
            .setStatus(CommonResWrapper.CommonRes.Status.newBuilder()
                    .setCode(0)
                    .setMsg("")
                    .setStatusType(CommonResWrapper.CommonRes.Status.ReqTp.SendMsg)
                    .build())
            .build();
    public static CommonResWrapper.CommonRes CREATE_GROUP_SUCCESS = CommonResWrapper.CommonRes.newBuilder()
            .setDataType(CommonResWrapper.CommonRes.DataType.StatusType)
            .setStatus(CommonResWrapper.CommonRes.Status.newBuilder()
                    .setCode(0)
                    .setMsg("")
                    .setStatusType(CommonResWrapper.CommonRes.Status.ReqTp.CreateGroup)
                    .build())
            .build();
    public static CommonResWrapper.CommonRes ADD_TO_GROUP_SUCCESS = CommonResWrapper.CommonRes.newBuilder()
            .setDataType(CommonResWrapper.CommonRes.DataType.StatusType)
            .setStatus(CommonResWrapper.CommonRes.Status.newBuilder()
                    .setCode(0)
                    .setMsg("")
                    .setStatusType(CommonResWrapper.CommonRes.Status.ReqTp.AddToGroup)
                    .build())
            .build();
    public static CommonResWrapper.CommonRes GROUP_EXISTS = CommonResWrapper.CommonRes.newBuilder()
            .setDataType(CommonResWrapper.CommonRes.DataType.StatusType)
            .setStatus(CommonResWrapper.CommonRes.Status.newBuilder()
                    .setCode(1)
                    .setMsg("该群已存在")
                    .setStatusType(CommonResWrapper.CommonRes.Status.ReqTp.CreateGroup)
                    .build())
            .build();
    public static CommonResWrapper.CommonRes GROUP_NOT_EXISTS = CommonResWrapper.CommonRes.newBuilder()
            .setDataType(CommonResWrapper.CommonRes.DataType.StatusType)
            .setStatus(CommonResWrapper.CommonRes.Status.newBuilder()
                    .setCode(1)
                    .setMsg("该群不存在")
                    .setStatusType(CommonResWrapper.CommonRes.Status.ReqTp.AddToGroup)
                    .build())
            .build();
}
