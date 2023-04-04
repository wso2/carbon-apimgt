package choreo.apis;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: user_service.proto")
public final class UserServiceGrpc {

  private UserServiceGrpc() {}

  public static final String SERVICE_NAME = "choreo.apis.UserService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.RegisterUserRequest,
      choreo.apis.UserServiceOuterClass.RegisterUserResponse> getRegisterUserMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RegisterUser",
      requestType = choreo.apis.UserServiceOuterClass.RegisterUserRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.RegisterUserResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.RegisterUserRequest,
      choreo.apis.UserServiceOuterClass.RegisterUserResponse> getRegisterUserMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.RegisterUserRequest, choreo.apis.UserServiceOuterClass.RegisterUserResponse> getRegisterUserMethod;
    if ((getRegisterUserMethod = UserServiceGrpc.getRegisterUserMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getRegisterUserMethod = UserServiceGrpc.getRegisterUserMethod) == null) {
          UserServiceGrpc.getRegisterUserMethod = getRegisterUserMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.RegisterUserRequest, choreo.apis.UserServiceOuterClass.RegisterUserResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "RegisterUser"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.RegisterUserRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.RegisterUserResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("RegisterUser"))
                  .build();
          }
        }
     }
     return getRegisterUserMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ValidateUserRequest,
      choreo.apis.UserServiceOuterClass.ValidateUserResponse> getValidateUserMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ValidateUser",
      requestType = choreo.apis.UserServiceOuterClass.ValidateUserRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.ValidateUserResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ValidateUserRequest,
      choreo.apis.UserServiceOuterClass.ValidateUserResponse> getValidateUserMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ValidateUserRequest, choreo.apis.UserServiceOuterClass.ValidateUserResponse> getValidateUserMethod;
    if ((getValidateUserMethod = UserServiceGrpc.getValidateUserMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getValidateUserMethod = UserServiceGrpc.getValidateUserMethod) == null) {
          UserServiceGrpc.getValidateUserMethod = getValidateUserMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.ValidateUserRequest, choreo.apis.UserServiceOuterClass.ValidateUserResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "ValidateUser"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ValidateUserRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ValidateUserResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("ValidateUser"))
                  .build();
          }
        }
     }
     return getValidateUserMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest,
      choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse> getFindUserFromIdpIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FindUserFromIdpId",
      requestType = choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest,
      choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse> getFindUserFromIdpIdMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest, choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse> getFindUserFromIdpIdMethod;
    if ((getFindUserFromIdpIdMethod = UserServiceGrpc.getFindUserFromIdpIdMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getFindUserFromIdpIdMethod = UserServiceGrpc.getFindUserFromIdpIdMethod) == null) {
          UserServiceGrpc.getFindUserFromIdpIdMethod = getFindUserFromIdpIdMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest, choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "FindUserFromIdpId"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("FindUserFromIdpId"))
                  .build();
          }
        }
     }
     return getFindUserFromIdpIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest,
      com.google.protobuf.Empty> getAddOrganizationUsersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddOrganizationUsers",
      requestType = choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest,
      com.google.protobuf.Empty> getAddOrganizationUsersMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest, com.google.protobuf.Empty> getAddOrganizationUsersMethod;
    if ((getAddOrganizationUsersMethod = UserServiceGrpc.getAddOrganizationUsersMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getAddOrganizationUsersMethod = UserServiceGrpc.getAddOrganizationUsersMethod) == null) {
          UserServiceGrpc.getAddOrganizationUsersMethod = getAddOrganizationUsersMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "AddOrganizationUsers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("AddOrganizationUsers"))
                  .build();
          }
        }
     }
     return getAddOrganizationUsersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest,
      com.google.protobuf.Empty> getDeleteOrganizationUserMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteOrganizationUser",
      requestType = choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest,
      com.google.protobuf.Empty> getDeleteOrganizationUserMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest, com.google.protobuf.Empty> getDeleteOrganizationUserMethod;
    if ((getDeleteOrganizationUserMethod = UserServiceGrpc.getDeleteOrganizationUserMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getDeleteOrganizationUserMethod = UserServiceGrpc.getDeleteOrganizationUserMethod) == null) {
          UserServiceGrpc.getDeleteOrganizationUserMethod = getDeleteOrganizationUserMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "DeleteOrganizationUser"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("DeleteOrganizationUser"))
                  .build();
          }
        }
     }
     return getDeleteOrganizationUserMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest,
      choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse> getFindUserOrganizationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FindUserOrganizations",
      requestType = choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest,
      choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse> getFindUserOrganizationsMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest, choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse> getFindUserOrganizationsMethod;
    if ((getFindUserOrganizationsMethod = UserServiceGrpc.getFindUserOrganizationsMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getFindUserOrganizationsMethod = UserServiceGrpc.getFindUserOrganizationsMethod) == null) {
          UserServiceGrpc.getFindUserOrganizationsMethod = getFindUserOrganizationsMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest, choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "FindUserOrganizations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("FindUserOrganizations"))
                  .build();
          }
        }
     }
     return getFindUserOrganizationsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest,
      choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse> getFindUsersByOrganizationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FindUsersByOrganization",
      requestType = choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest,
      choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse> getFindUsersByOrganizationMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest, choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse> getFindUsersByOrganizationMethod;
    if ((getFindUsersByOrganizationMethod = UserServiceGrpc.getFindUsersByOrganizationMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getFindUsersByOrganizationMethod = UserServiceGrpc.getFindUsersByOrganizationMethod) == null) {
          UserServiceGrpc.getFindUsersByOrganizationMethod = getFindUsersByOrganizationMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest, choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "FindUsersByOrganization"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("FindUsersByOrganization"))
                  .build();
          }
        }
     }
     return getFindUsersByOrganizationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest,
      choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse> getFindUsersByOrganizationByRoleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FindUsersByOrganizationByRole",
      requestType = choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest,
      choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse> getFindUsersByOrganizationByRoleMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest, choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse> getFindUsersByOrganizationByRoleMethod;
    if ((getFindUsersByOrganizationByRoleMethod = UserServiceGrpc.getFindUsersByOrganizationByRoleMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getFindUsersByOrganizationByRoleMethod = UserServiceGrpc.getFindUsersByOrganizationByRoleMethod) == null) {
          UserServiceGrpc.getFindUsersByOrganizationByRoleMethod = getFindUsersByOrganizationByRoleMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest, choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "FindUsersByOrganizationByRole"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("FindUsersByOrganizationByRole"))
                  .build();
          }
        }
     }
     return getFindUsersByOrganizationByRoleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest,
      choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse> getCreateUserFeedbackMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateUserFeedback",
      requestType = choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest,
      choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse> getCreateUserFeedbackMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest, choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse> getCreateUserFeedbackMethod;
    if ((getCreateUserFeedbackMethod = UserServiceGrpc.getCreateUserFeedbackMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getCreateUserFeedbackMethod = UserServiceGrpc.getCreateUserFeedbackMethod) == null) {
          UserServiceGrpc.getCreateUserFeedbackMethod = getCreateUserFeedbackMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest, choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "CreateUserFeedback"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("CreateUserFeedback"))
                  .build();
          }
        }
     }
     return getCreateUserFeedbackMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetUserInfoRequest,
      choreo.apis.UserServiceOuterClass.GetUserInfoResponse> getGetUserInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserInfo",
      requestType = choreo.apis.UserServiceOuterClass.GetUserInfoRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.GetUserInfoResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetUserInfoRequest,
      choreo.apis.UserServiceOuterClass.GetUserInfoResponse> getGetUserInfoMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetUserInfoRequest, choreo.apis.UserServiceOuterClass.GetUserInfoResponse> getGetUserInfoMethod;
    if ((getGetUserInfoMethod = UserServiceGrpc.getGetUserInfoMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getGetUserInfoMethod = UserServiceGrpc.getGetUserInfoMethod) == null) {
          UserServiceGrpc.getGetUserInfoMethod = getGetUserInfoMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.GetUserInfoRequest, choreo.apis.UserServiceOuterClass.GetUserInfoResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "GetUserInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetUserInfoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetUserInfoResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("GetUserInfo"))
                  .build();
          }
        }
     }
     return getGetUserInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetUserRolesRequest,
      choreo.apis.UserServiceOuterClass.GetUserRolesResponse> getGetUserRolesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserRoles",
      requestType = choreo.apis.UserServiceOuterClass.GetUserRolesRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.GetUserRolesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetUserRolesRequest,
      choreo.apis.UserServiceOuterClass.GetUserRolesResponse> getGetUserRolesMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetUserRolesRequest, choreo.apis.UserServiceOuterClass.GetUserRolesResponse> getGetUserRolesMethod;
    if ((getGetUserRolesMethod = UserServiceGrpc.getGetUserRolesMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getGetUserRolesMethod = UserServiceGrpc.getGetUserRolesMethod) == null) {
          UserServiceGrpc.getGetUserRolesMethod = getGetUserRolesMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.GetUserRolesRequest, choreo.apis.UserServiceOuterClass.GetUserRolesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "GetUserRoles"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetUserRolesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetUserRolesResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("GetUserRoles"))
                  .build();
          }
        }
     }
     return getGetUserRolesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest,
      choreo.apis.UserServiceOuterClass.GetUserRolesResponse> getGetEnterpriseUserRolesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetEnterpriseUserRoles",
      requestType = choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.GetUserRolesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest,
      choreo.apis.UserServiceOuterClass.GetUserRolesResponse> getGetEnterpriseUserRolesMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest, choreo.apis.UserServiceOuterClass.GetUserRolesResponse> getGetEnterpriseUserRolesMethod;
    if ((getGetEnterpriseUserRolesMethod = UserServiceGrpc.getGetEnterpriseUserRolesMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getGetEnterpriseUserRolesMethod = UserServiceGrpc.getGetEnterpriseUserRolesMethod) == null) {
          UserServiceGrpc.getGetEnterpriseUserRolesMethod = getGetEnterpriseUserRolesMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest, choreo.apis.UserServiceOuterClass.GetUserRolesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "GetEnterpriseUserRoles"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetUserRolesResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("GetEnterpriseUserRoles"))
                  .build();
          }
        }
     }
     return getGetEnterpriseUserRolesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest,
      choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse> getUpdateMembersOfRoleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateMembersOfRole",
      requestType = choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest,
      choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse> getUpdateMembersOfRoleMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest, choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse> getUpdateMembersOfRoleMethod;
    if ((getUpdateMembersOfRoleMethod = UserServiceGrpc.getUpdateMembersOfRoleMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getUpdateMembersOfRoleMethod = UserServiceGrpc.getUpdateMembersOfRoleMethod) == null) {
          UserServiceGrpc.getUpdateMembersOfRoleMethod = getUpdateMembersOfRoleMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest, choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "UpdateMembersOfRole"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("UpdateMembersOfRole"))
                  .build();
          }
        }
     }
     return getUpdateMembersOfRoleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest,
      choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse> getUpdateRolesOfMemberMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateRolesOfMember",
      requestType = choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest,
      choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse> getUpdateRolesOfMemberMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest, choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse> getUpdateRolesOfMemberMethod;
    if ((getUpdateRolesOfMemberMethod = UserServiceGrpc.getUpdateRolesOfMemberMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getUpdateRolesOfMemberMethod = UserServiceGrpc.getUpdateRolesOfMemberMethod) == null) {
          UserServiceGrpc.getUpdateRolesOfMemberMethod = getUpdateRolesOfMemberMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest, choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "UpdateRolesOfMember"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("UpdateRolesOfMember"))
                  .build();
          }
        }
     }
     return getUpdateRolesOfMemberMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest,
      choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse> getStartInvitationFlowMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StartInvitationFlow",
      requestType = choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest,
      choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse> getStartInvitationFlowMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest, choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse> getStartInvitationFlowMethod;
    if ((getStartInvitationFlowMethod = UserServiceGrpc.getStartInvitationFlowMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getStartInvitationFlowMethod = UserServiceGrpc.getStartInvitationFlowMethod) == null) {
          UserServiceGrpc.getStartInvitationFlowMethod = getStartInvitationFlowMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest, choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "StartInvitationFlow"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("StartInvitationFlow"))
                  .build();
          }
        }
     }
     return getStartInvitationFlowMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateOrganizationRequest,
      choreo.apis.UserServiceOuterClass.CreateOrganizationResponse> getCreateOrganizationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateOrganization",
      requestType = choreo.apis.UserServiceOuterClass.CreateOrganizationRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.CreateOrganizationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateOrganizationRequest,
      choreo.apis.UserServiceOuterClass.CreateOrganizationResponse> getCreateOrganizationMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateOrganizationRequest, choreo.apis.UserServiceOuterClass.CreateOrganizationResponse> getCreateOrganizationMethod;
    if ((getCreateOrganizationMethod = UserServiceGrpc.getCreateOrganizationMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getCreateOrganizationMethod = UserServiceGrpc.getCreateOrganizationMethod) == null) {
          UserServiceGrpc.getCreateOrganizationMethod = getCreateOrganizationMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.CreateOrganizationRequest, choreo.apis.UserServiceOuterClass.CreateOrganizationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "CreateOrganization"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateOrganizationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateOrganizationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("CreateOrganization"))
                  .build();
          }
        }
     }
     return getCreateOrganizationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetOrganizationRequest,
      choreo.apis.UserServiceOuterClass.GetOrganizationResponse> getGetOrganizationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetOrganization",
      requestType = choreo.apis.UserServiceOuterClass.GetOrganizationRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.GetOrganizationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetOrganizationRequest,
      choreo.apis.UserServiceOuterClass.GetOrganizationResponse> getGetOrganizationMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetOrganizationRequest, choreo.apis.UserServiceOuterClass.GetOrganizationResponse> getGetOrganizationMethod;
    if ((getGetOrganizationMethod = UserServiceGrpc.getGetOrganizationMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getGetOrganizationMethod = UserServiceGrpc.getGetOrganizationMethod) == null) {
          UserServiceGrpc.getGetOrganizationMethod = getGetOrganizationMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.GetOrganizationRequest, choreo.apis.UserServiceOuterClass.GetOrganizationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "GetOrganization"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetOrganizationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetOrganizationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("GetOrganization"))
                  .build();
          }
        }
     }
     return getGetOrganizationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest,
      choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse> getUpdateOrganizationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateOrganization",
      requestType = choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest,
      choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse> getUpdateOrganizationMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest, choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse> getUpdateOrganizationMethod;
    if ((getUpdateOrganizationMethod = UserServiceGrpc.getUpdateOrganizationMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getUpdateOrganizationMethod = UserServiceGrpc.getUpdateOrganizationMethod) == null) {
          UserServiceGrpc.getUpdateOrganizationMethod = getUpdateOrganizationMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest, choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "UpdateOrganization"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("UpdateOrganization"))
                  .build();
          }
        }
     }
     return getUpdateOrganizationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest,
      choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse> getDeleteOrganizationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteOrganization",
      requestType = choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest,
      choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse> getDeleteOrganizationMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest, choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse> getDeleteOrganizationMethod;
    if ((getDeleteOrganizationMethod = UserServiceGrpc.getDeleteOrganizationMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getDeleteOrganizationMethod = UserServiceGrpc.getDeleteOrganizationMethod) == null) {
          UserServiceGrpc.getDeleteOrganizationMethod = getDeleteOrganizationMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest, choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "DeleteOrganization"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("DeleteOrganization"))
                  .build();
          }
        }
     }
     return getDeleteOrganizationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListOrganizationsRequest,
      choreo.apis.UserServiceOuterClass.ListOrganizationsResponse> getListOrganizationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListOrganizations",
      requestType = choreo.apis.UserServiceOuterClass.ListOrganizationsRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.ListOrganizationsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListOrganizationsRequest,
      choreo.apis.UserServiceOuterClass.ListOrganizationsResponse> getListOrganizationsMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListOrganizationsRequest, choreo.apis.UserServiceOuterClass.ListOrganizationsResponse> getListOrganizationsMethod;
    if ((getListOrganizationsMethod = UserServiceGrpc.getListOrganizationsMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getListOrganizationsMethod = UserServiceGrpc.getListOrganizationsMethod) == null) {
          UserServiceGrpc.getListOrganizationsMethod = getListOrganizationsMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.ListOrganizationsRequest, choreo.apis.UserServiceOuterClass.ListOrganizationsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "ListOrganizations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListOrganizationsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListOrganizationsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("ListOrganizations"))
                  .build();
          }
        }
     }
     return getListOrganizationsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest,
      choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse> getListOrganizationEnvIdsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListOrganizationEnvIds",
      requestType = choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest,
      choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse> getListOrganizationEnvIdsMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest, choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse> getListOrganizationEnvIdsMethod;
    if ((getListOrganizationEnvIdsMethod = UserServiceGrpc.getListOrganizationEnvIdsMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getListOrganizationEnvIdsMethod = UserServiceGrpc.getListOrganizationEnvIdsMethod) == null) {
          UserServiceGrpc.getListOrganizationEnvIdsMethod = getListOrganizationEnvIdsMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest, choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "ListOrganizationEnvIds"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("ListOrganizationEnvIds"))
                  .build();
          }
        }
     }
     return getListOrganizationEnvIdsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest,
      choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse> getCreateDefaultEnvironmentsForOrgsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateDefaultEnvironmentsForOrgs",
      requestType = choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest,
      choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse> getCreateDefaultEnvironmentsForOrgsMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest, choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse> getCreateDefaultEnvironmentsForOrgsMethod;
    if ((getCreateDefaultEnvironmentsForOrgsMethod = UserServiceGrpc.getCreateDefaultEnvironmentsForOrgsMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getCreateDefaultEnvironmentsForOrgsMethod = UserServiceGrpc.getCreateDefaultEnvironmentsForOrgsMethod) == null) {
          UserServiceGrpc.getCreateDefaultEnvironmentsForOrgsMethod = getCreateDefaultEnvironmentsForOrgsMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest, choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "CreateDefaultEnvironmentsForOrgs"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("CreateDefaultEnvironmentsForOrgs"))
                  .build();
          }
        }
     }
     return getCreateDefaultEnvironmentsForOrgsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateGroupRequest,
      choreo.apis.UserServiceOuterClass.CreateGroupResponse> getCreateGroupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateGroup",
      requestType = choreo.apis.UserServiceOuterClass.CreateGroupRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.CreateGroupResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateGroupRequest,
      choreo.apis.UserServiceOuterClass.CreateGroupResponse> getCreateGroupMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateGroupRequest, choreo.apis.UserServiceOuterClass.CreateGroupResponse> getCreateGroupMethod;
    if ((getCreateGroupMethod = UserServiceGrpc.getCreateGroupMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getCreateGroupMethod = UserServiceGrpc.getCreateGroupMethod) == null) {
          UserServiceGrpc.getCreateGroupMethod = getCreateGroupMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.CreateGroupRequest, choreo.apis.UserServiceOuterClass.CreateGroupResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "CreateGroup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateGroupRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateGroupResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("CreateGroup"))
                  .build();
          }
        }
     }
     return getCreateGroupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateGroupRequest,
      choreo.apis.UserServiceOuterClass.UpdateGroupResponse> getUpdateGroupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateGroup",
      requestType = choreo.apis.UserServiceOuterClass.UpdateGroupRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.UpdateGroupResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateGroupRequest,
      choreo.apis.UserServiceOuterClass.UpdateGroupResponse> getUpdateGroupMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateGroupRequest, choreo.apis.UserServiceOuterClass.UpdateGroupResponse> getUpdateGroupMethod;
    if ((getUpdateGroupMethod = UserServiceGrpc.getUpdateGroupMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getUpdateGroupMethod = UserServiceGrpc.getUpdateGroupMethod) == null) {
          UserServiceGrpc.getUpdateGroupMethod = getUpdateGroupMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.UpdateGroupRequest, choreo.apis.UserServiceOuterClass.UpdateGroupResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "UpdateGroup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateGroupRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateGroupResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("UpdateGroup"))
                  .build();
          }
        }
     }
     return getUpdateGroupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListGroupsRequest,
      choreo.apis.UserServiceOuterClass.ListGroupsResponse> getListGroupsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListGroups",
      requestType = choreo.apis.UserServiceOuterClass.ListGroupsRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.ListGroupsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListGroupsRequest,
      choreo.apis.UserServiceOuterClass.ListGroupsResponse> getListGroupsMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListGroupsRequest, choreo.apis.UserServiceOuterClass.ListGroupsResponse> getListGroupsMethod;
    if ((getListGroupsMethod = UserServiceGrpc.getListGroupsMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getListGroupsMethod = UserServiceGrpc.getListGroupsMethod) == null) {
          UserServiceGrpc.getListGroupsMethod = getListGroupsMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.ListGroupsRequest, choreo.apis.UserServiceOuterClass.ListGroupsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "ListGroups"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListGroupsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListGroupsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("ListGroups"))
                  .build();
          }
        }
     }
     return getListGroupsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListUserGroupsRequest,
      choreo.apis.UserServiceOuterClass.ListUserGroupsResponse> getListUserGroupsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListUserGroups",
      requestType = choreo.apis.UserServiceOuterClass.ListUserGroupsRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.ListUserGroupsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListUserGroupsRequest,
      choreo.apis.UserServiceOuterClass.ListUserGroupsResponse> getListUserGroupsMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListUserGroupsRequest, choreo.apis.UserServiceOuterClass.ListUserGroupsResponse> getListUserGroupsMethod;
    if ((getListUserGroupsMethod = UserServiceGrpc.getListUserGroupsMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getListUserGroupsMethod = UserServiceGrpc.getListUserGroupsMethod) == null) {
          UserServiceGrpc.getListUserGroupsMethod = getListUserGroupsMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.ListUserGroupsRequest, choreo.apis.UserServiceOuterClass.ListUserGroupsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "ListUserGroups"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListUserGroupsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListUserGroupsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("ListUserGroups"))
                  .build();
          }
        }
     }
     return getListUserGroupsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetGroupInfoRequest,
      choreo.apis.UserServiceOuterClass.GetGroupInfoResponse> getGetGroupInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetGroupInfo",
      requestType = choreo.apis.UserServiceOuterClass.GetGroupInfoRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.GetGroupInfoResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetGroupInfoRequest,
      choreo.apis.UserServiceOuterClass.GetGroupInfoResponse> getGetGroupInfoMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetGroupInfoRequest, choreo.apis.UserServiceOuterClass.GetGroupInfoResponse> getGetGroupInfoMethod;
    if ((getGetGroupInfoMethod = UserServiceGrpc.getGetGroupInfoMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getGetGroupInfoMethod = UserServiceGrpc.getGetGroupInfoMethod) == null) {
          UserServiceGrpc.getGetGroupInfoMethod = getGetGroupInfoMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.GetGroupInfoRequest, choreo.apis.UserServiceOuterClass.GetGroupInfoResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "GetGroupInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetGroupInfoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetGroupInfoResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("GetGroupInfo"))
                  .build();
          }
        }
     }
     return getGetGroupInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteGroupRequest,
      choreo.apis.UserServiceOuterClass.DeleteGroupResponse> getDeleteGroupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteGroup",
      requestType = choreo.apis.UserServiceOuterClass.DeleteGroupRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.DeleteGroupResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteGroupRequest,
      choreo.apis.UserServiceOuterClass.DeleteGroupResponse> getDeleteGroupMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteGroupRequest, choreo.apis.UserServiceOuterClass.DeleteGroupResponse> getDeleteGroupMethod;
    if ((getDeleteGroupMethod = UserServiceGrpc.getDeleteGroupMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getDeleteGroupMethod = UserServiceGrpc.getDeleteGroupMethod) == null) {
          UserServiceGrpc.getDeleteGroupMethod = getDeleteGroupMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.DeleteGroupRequest, choreo.apis.UserServiceOuterClass.DeleteGroupResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "DeleteGroup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.DeleteGroupRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.DeleteGroupResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("DeleteGroup"))
                  .build();
          }
        }
     }
     return getDeleteGroupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest,
      choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse> getRemoveMemberFromGroupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RemoveMemberFromGroup",
      requestType = choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest,
      choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse> getRemoveMemberFromGroupMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest, choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse> getRemoveMemberFromGroupMethod;
    if ((getRemoveMemberFromGroupMethod = UserServiceGrpc.getRemoveMemberFromGroupMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getRemoveMemberFromGroupMethod = UserServiceGrpc.getRemoveMemberFromGroupMethod) == null) {
          UserServiceGrpc.getRemoveMemberFromGroupMethod = getRemoveMemberFromGroupMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest, choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "RemoveMemberFromGroup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("RemoveMemberFromGroup"))
                  .build();
          }
        }
     }
     return getRemoveMemberFromGroupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateRoleRequest,
      choreo.apis.UserServiceOuterClass.CreateRoleResponse> getCreateRoleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateRole",
      requestType = choreo.apis.UserServiceOuterClass.CreateRoleRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.CreateRoleResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateRoleRequest,
      choreo.apis.UserServiceOuterClass.CreateRoleResponse> getCreateRoleMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.CreateRoleRequest, choreo.apis.UserServiceOuterClass.CreateRoleResponse> getCreateRoleMethod;
    if ((getCreateRoleMethod = UserServiceGrpc.getCreateRoleMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getCreateRoleMethod = UserServiceGrpc.getCreateRoleMethod) == null) {
          UserServiceGrpc.getCreateRoleMethod = getCreateRoleMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.CreateRoleRequest, choreo.apis.UserServiceOuterClass.CreateRoleResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "CreateRole"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateRoleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.CreateRoleResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("CreateRole"))
                  .build();
          }
        }
     }
     return getCreateRoleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateRoleRequest,
      choreo.apis.UserServiceOuterClass.UpdateRoleResponse> getUpdateRoleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateRole",
      requestType = choreo.apis.UserServiceOuterClass.UpdateRoleRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.UpdateRoleResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateRoleRequest,
      choreo.apis.UserServiceOuterClass.UpdateRoleResponse> getUpdateRoleMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.UpdateRoleRequest, choreo.apis.UserServiceOuterClass.UpdateRoleResponse> getUpdateRoleMethod;
    if ((getUpdateRoleMethod = UserServiceGrpc.getUpdateRoleMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getUpdateRoleMethod = UserServiceGrpc.getUpdateRoleMethod) == null) {
          UserServiceGrpc.getUpdateRoleMethod = getUpdateRoleMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.UpdateRoleRequest, choreo.apis.UserServiceOuterClass.UpdateRoleResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "UpdateRole"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateRoleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.UpdateRoleResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("UpdateRole"))
                  .build();
          }
        }
     }
     return getUpdateRoleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetRoleInfoRequest,
      choreo.apis.UserServiceOuterClass.GetRoleInfoResponse> getGetRoleInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetRoleInfo",
      requestType = choreo.apis.UserServiceOuterClass.GetRoleInfoRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.GetRoleInfoResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetRoleInfoRequest,
      choreo.apis.UserServiceOuterClass.GetRoleInfoResponse> getGetRoleInfoMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetRoleInfoRequest, choreo.apis.UserServiceOuterClass.GetRoleInfoResponse> getGetRoleInfoMethod;
    if ((getGetRoleInfoMethod = UserServiceGrpc.getGetRoleInfoMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getGetRoleInfoMethod = UserServiceGrpc.getGetRoleInfoMethod) == null) {
          UserServiceGrpc.getGetRoleInfoMethod = getGetRoleInfoMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.GetRoleInfoRequest, choreo.apis.UserServiceOuterClass.GetRoleInfoResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "GetRoleInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetRoleInfoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetRoleInfoResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("GetRoleInfo"))
                  .build();
          }
        }
     }
     return getGetRoleInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteRoleRequest,
      choreo.apis.UserServiceOuterClass.DeleteRoleResponse> getDeleteRoleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteRole",
      requestType = choreo.apis.UserServiceOuterClass.DeleteRoleRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.DeleteRoleResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteRoleRequest,
      choreo.apis.UserServiceOuterClass.DeleteRoleResponse> getDeleteRoleMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteRoleRequest, choreo.apis.UserServiceOuterClass.DeleteRoleResponse> getDeleteRoleMethod;
    if ((getDeleteRoleMethod = UserServiceGrpc.getDeleteRoleMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getDeleteRoleMethod = UserServiceGrpc.getDeleteRoleMethod) == null) {
          UserServiceGrpc.getDeleteRoleMethod = getDeleteRoleMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.DeleteRoleRequest, choreo.apis.UserServiceOuterClass.DeleteRoleResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "DeleteRole"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.DeleteRoleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.DeleteRoleResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("DeleteRole"))
                  .build();
          }
        }
     }
     return getDeleteRoleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListRolesRequest,
      choreo.apis.UserServiceOuterClass.ListRolesResponse> getListRolesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListRoles",
      requestType = choreo.apis.UserServiceOuterClass.ListRolesRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.ListRolesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListRolesRequest,
      choreo.apis.UserServiceOuterClass.ListRolesResponse> getListRolesMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListRolesRequest, choreo.apis.UserServiceOuterClass.ListRolesResponse> getListRolesMethod;
    if ((getListRolesMethod = UserServiceGrpc.getListRolesMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getListRolesMethod = UserServiceGrpc.getListRolesMethod) == null) {
          UserServiceGrpc.getListRolesMethod = getListRolesMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.ListRolesRequest, choreo.apis.UserServiceOuterClass.ListRolesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "ListRoles"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListRolesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListRolesResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("ListRoles"))
                  .build();
          }
        }
     }
     return getListRolesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetPermissionListRequest,
      choreo.apis.UserServiceOuterClass.GetPermissionListResponse> getGetPermissionListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPermissionList",
      requestType = choreo.apis.UserServiceOuterClass.GetPermissionListRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.GetPermissionListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetPermissionListRequest,
      choreo.apis.UserServiceOuterClass.GetPermissionListResponse> getGetPermissionListMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.GetPermissionListRequest, choreo.apis.UserServiceOuterClass.GetPermissionListResponse> getGetPermissionListMethod;
    if ((getGetPermissionListMethod = UserServiceGrpc.getGetPermissionListMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getGetPermissionListMethod = UserServiceGrpc.getGetPermissionListMethod) == null) {
          UserServiceGrpc.getGetPermissionListMethod = getGetPermissionListMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.GetPermissionListRequest, choreo.apis.UserServiceOuterClass.GetPermissionListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "GetPermissionList"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetPermissionListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.GetPermissionListResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("GetPermissionList"))
                  .build();
          }
        }
     }
     return getGetPermissionListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest,
      choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse> getSendMemberInvitationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendMemberInvitation",
      requestType = choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest,
      choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse> getSendMemberInvitationMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest, choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse> getSendMemberInvitationMethod;
    if ((getSendMemberInvitationMethod = UserServiceGrpc.getSendMemberInvitationMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getSendMemberInvitationMethod = UserServiceGrpc.getSendMemberInvitationMethod) == null) {
          UserServiceGrpc.getSendMemberInvitationMethod = getSendMemberInvitationMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest, choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "SendMemberInvitation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("SendMemberInvitation"))
                  .build();
          }
        }
     }
     return getSendMemberInvitationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest,
      choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse> getAcceptMemberInvitationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AcceptMemberInvitation",
      requestType = choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest,
      choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse> getAcceptMemberInvitationMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest, choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse> getAcceptMemberInvitationMethod;
    if ((getAcceptMemberInvitationMethod = UserServiceGrpc.getAcceptMemberInvitationMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getAcceptMemberInvitationMethod = UserServiceGrpc.getAcceptMemberInvitationMethod) == null) {
          UserServiceGrpc.getAcceptMemberInvitationMethod = getAcceptMemberInvitationMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest, choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "AcceptMemberInvitation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("AcceptMemberInvitation"))
                  .build();
          }
        }
     }
     return getAcceptMemberInvitationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest,
      choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse> getFindMemberInvitationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FindMemberInvitation",
      requestType = choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest,
      choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse> getFindMemberInvitationMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest, choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse> getFindMemberInvitationMethod;
    if ((getFindMemberInvitationMethod = UserServiceGrpc.getFindMemberInvitationMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getFindMemberInvitationMethod = UserServiceGrpc.getFindMemberInvitationMethod) == null) {
          UserServiceGrpc.getFindMemberInvitationMethod = getFindMemberInvitationMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest, choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "FindMemberInvitation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("FindMemberInvitation"))
                  .build();
          }
        }
     }
     return getFindMemberInvitationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest,
      choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse> getDeleteMemberInvitationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteMemberInvitation",
      requestType = choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest,
      choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse> getDeleteMemberInvitationMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest, choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse> getDeleteMemberInvitationMethod;
    if ((getDeleteMemberInvitationMethod = UserServiceGrpc.getDeleteMemberInvitationMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getDeleteMemberInvitationMethod = UserServiceGrpc.getDeleteMemberInvitationMethod) == null) {
          UserServiceGrpc.getDeleteMemberInvitationMethod = getDeleteMemberInvitationMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest, choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "DeleteMemberInvitation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("DeleteMemberInvitation"))
                  .build();
          }
        }
     }
     return getDeleteMemberInvitationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest,
      choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse> getListMemberInvitationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListMemberInvitations",
      requestType = choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest.class,
      responseType = choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest,
      choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse> getListMemberInvitationsMethod() {
    io.grpc.MethodDescriptor<choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest, choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse> getListMemberInvitationsMethod;
    if ((getListMemberInvitationsMethod = UserServiceGrpc.getListMemberInvitationsMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getListMemberInvitationsMethod = UserServiceGrpc.getListMemberInvitationsMethod) == null) {
          UserServiceGrpc.getListMemberInvitationsMethod = getListMemberInvitationsMethod = 
              io.grpc.MethodDescriptor.<choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest, choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "choreo.apis.UserService", "ListMemberInvitations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("ListMemberInvitations"))
                  .build();
          }
        }
     }
     return getListMemberInvitationsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UserServiceStub newStub(io.grpc.Channel channel) {
    return new UserServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UserServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new UserServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UserServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new UserServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class UserServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void registerUser(choreo.apis.UserServiceOuterClass.RegisterUserRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.RegisterUserResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRegisterUserMethod(), responseObserver);
    }

    /**
     */
    public void validateUser(choreo.apis.UserServiceOuterClass.ValidateUserRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ValidateUserResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getValidateUserMethod(), responseObserver);
    }

    /**
     */
    public void findUserFromIdpId(choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getFindUserFromIdpIdMethod(), responseObserver);
    }

    /**
     */
    public void addOrganizationUsers(choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getAddOrganizationUsersMethod(), responseObserver);
    }

    /**
     */
    public void deleteOrganizationUser(choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteOrganizationUserMethod(), responseObserver);
    }

    /**
     */
    public void findUserOrganizations(choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getFindUserOrganizationsMethod(), responseObserver);
    }

    /**
     */
    public void findUsersByOrganization(choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getFindUsersByOrganizationMethod(), responseObserver);
    }

    /**
     */
    public void findUsersByOrganizationByRole(choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getFindUsersByOrganizationByRoleMethod(), responseObserver);
    }

    /**
     * <pre>
     * todo: increase rpc payload size to 10mb
     * </pre>
     */
    public void createUserFeedback(choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateUserFeedbackMethod(), responseObserver);
    }

    /**
     */
    public void getUserInfo(choreo.apis.UserServiceOuterClass.GetUserInfoRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetUserInfoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetUserInfoMethod(), responseObserver);
    }

    /**
     */
    public void getUserRoles(choreo.apis.UserServiceOuterClass.GetUserRolesRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetUserRolesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetUserRolesMethod(), responseObserver);
    }

    /**
     */
    public void getEnterpriseUserRoles(choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetUserRolesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetEnterpriseUserRolesMethod(), responseObserver);
    }

    /**
     */
    public void updateMembersOfRole(choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUpdateMembersOfRoleMethod(), responseObserver);
    }

    /**
     */
    public void updateRolesOfMember(choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUpdateRolesOfMemberMethod(), responseObserver);
    }

    /**
     */
    public void startInvitationFlow(choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getStartInvitationFlowMethod(), responseObserver);
    }

    /**
     * <pre>
     * Organizations
     * </pre>
     */
    public void createOrganization(choreo.apis.UserServiceOuterClass.CreateOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateOrganizationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateOrganizationMethod(), responseObserver);
    }

    /**
     */
    public void getOrganization(choreo.apis.UserServiceOuterClass.GetOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetOrganizationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetOrganizationMethod(), responseObserver);
    }

    /**
     */
    public void updateOrganization(choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUpdateOrganizationMethod(), responseObserver);
    }

    /**
     */
    public void deleteOrganization(choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteOrganizationMethod(), responseObserver);
    }

    /**
     */
    public void listOrganizations(choreo.apis.UserServiceOuterClass.ListOrganizationsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListOrganizationsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getListOrganizationsMethod(), responseObserver);
    }

    /**
     */
    public void listOrganizationEnvIds(choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getListOrganizationEnvIdsMethod(), responseObserver);
    }

    /**
     */
    public void createDefaultEnvironmentsForOrgs(choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateDefaultEnvironmentsForOrgsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Groups
     * </pre>
     */
    public void createGroup(choreo.apis.UserServiceOuterClass.CreateGroupRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateGroupResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateGroupMethod(), responseObserver);
    }

    /**
     */
    public void updateGroup(choreo.apis.UserServiceOuterClass.UpdateGroupRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateGroupResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUpdateGroupMethod(), responseObserver);
    }

    /**
     */
    public void listGroups(choreo.apis.UserServiceOuterClass.ListGroupsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListGroupsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getListGroupsMethod(), responseObserver);
    }

    /**
     */
    public void listUserGroups(choreo.apis.UserServiceOuterClass.ListUserGroupsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListUserGroupsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getListUserGroupsMethod(), responseObserver);
    }

    /**
     */
    public void getGroupInfo(choreo.apis.UserServiceOuterClass.GetGroupInfoRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetGroupInfoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetGroupInfoMethod(), responseObserver);
    }

    /**
     */
    public void deleteGroup(choreo.apis.UserServiceOuterClass.DeleteGroupRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteGroupResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteGroupMethod(), responseObserver);
    }

    /**
     */
    public void removeMemberFromGroup(choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRemoveMemberFromGroupMethod(), responseObserver);
    }

    /**
     * <pre>
     * Roles
     * </pre>
     */
    public void createRole(choreo.apis.UserServiceOuterClass.CreateRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateRoleResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateRoleMethod(), responseObserver);
    }

    /**
     */
    public void updateRole(choreo.apis.UserServiceOuterClass.UpdateRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateRoleResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUpdateRoleMethod(), responseObserver);
    }

    /**
     */
    public void getRoleInfo(choreo.apis.UserServiceOuterClass.GetRoleInfoRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetRoleInfoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetRoleInfoMethod(), responseObserver);
    }

    /**
     */
    public void deleteRole(choreo.apis.UserServiceOuterClass.DeleteRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteRoleResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteRoleMethod(), responseObserver);
    }

    /**
     */
    public void listRoles(choreo.apis.UserServiceOuterClass.ListRolesRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListRolesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getListRolesMethod(), responseObserver);
    }

    /**
     */
    public void getPermissionList(choreo.apis.UserServiceOuterClass.GetPermissionListRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetPermissionListResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetPermissionListMethod(), responseObserver);
    }

    /**
     * <pre>
     * Org-member invitations
     * </pre>
     */
    public void sendMemberInvitation(choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getSendMemberInvitationMethod(), responseObserver);
    }

    /**
     */
    public void acceptMemberInvitation(choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getAcceptMemberInvitationMethod(), responseObserver);
    }

    /**
     */
    public void findMemberInvitation(choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getFindMemberInvitationMethod(), responseObserver);
    }

    /**
     */
    public void deleteMemberInvitation(choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteMemberInvitationMethod(), responseObserver);
    }

    /**
     */
    public void listMemberInvitations(choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getListMemberInvitationsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRegisterUserMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.RegisterUserRequest,
                choreo.apis.UserServiceOuterClass.RegisterUserResponse>(
                  this, METHODID_REGISTER_USER)))
          .addMethod(
            getValidateUserMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.ValidateUserRequest,
                choreo.apis.UserServiceOuterClass.ValidateUserResponse>(
                  this, METHODID_VALIDATE_USER)))
          .addMethod(
            getFindUserFromIdpIdMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest,
                choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse>(
                  this, METHODID_FIND_USER_FROM_IDP_ID)))
          .addMethod(
            getAddOrganizationUsersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_ADD_ORGANIZATION_USERS)))
          .addMethod(
            getDeleteOrganizationUserMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_DELETE_ORGANIZATION_USER)))
          .addMethod(
            getFindUserOrganizationsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest,
                choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse>(
                  this, METHODID_FIND_USER_ORGANIZATIONS)))
          .addMethod(
            getFindUsersByOrganizationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest,
                choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse>(
                  this, METHODID_FIND_USERS_BY_ORGANIZATION)))
          .addMethod(
            getFindUsersByOrganizationByRoleMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest,
                choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse>(
                  this, METHODID_FIND_USERS_BY_ORGANIZATION_BY_ROLE)))
          .addMethod(
            getCreateUserFeedbackMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest,
                choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse>(
                  this, METHODID_CREATE_USER_FEEDBACK)))
          .addMethod(
            getGetUserInfoMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.GetUserInfoRequest,
                choreo.apis.UserServiceOuterClass.GetUserInfoResponse>(
                  this, METHODID_GET_USER_INFO)))
          .addMethod(
            getGetUserRolesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.GetUserRolesRequest,
                choreo.apis.UserServiceOuterClass.GetUserRolesResponse>(
                  this, METHODID_GET_USER_ROLES)))
          .addMethod(
            getGetEnterpriseUserRolesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest,
                choreo.apis.UserServiceOuterClass.GetUserRolesResponse>(
                  this, METHODID_GET_ENTERPRISE_USER_ROLES)))
          .addMethod(
            getUpdateMembersOfRoleMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest,
                choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse>(
                  this, METHODID_UPDATE_MEMBERS_OF_ROLE)))
          .addMethod(
            getUpdateRolesOfMemberMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest,
                choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse>(
                  this, METHODID_UPDATE_ROLES_OF_MEMBER)))
          .addMethod(
            getStartInvitationFlowMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest,
                choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse>(
                  this, METHODID_START_INVITATION_FLOW)))
          .addMethod(
            getCreateOrganizationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.CreateOrganizationRequest,
                choreo.apis.UserServiceOuterClass.CreateOrganizationResponse>(
                  this, METHODID_CREATE_ORGANIZATION)))
          .addMethod(
            getGetOrganizationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.GetOrganizationRequest,
                choreo.apis.UserServiceOuterClass.GetOrganizationResponse>(
                  this, METHODID_GET_ORGANIZATION)))
          .addMethod(
            getUpdateOrganizationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest,
                choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse>(
                  this, METHODID_UPDATE_ORGANIZATION)))
          .addMethod(
            getDeleteOrganizationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest,
                choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse>(
                  this, METHODID_DELETE_ORGANIZATION)))
          .addMethod(
            getListOrganizationsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.ListOrganizationsRequest,
                choreo.apis.UserServiceOuterClass.ListOrganizationsResponse>(
                  this, METHODID_LIST_ORGANIZATIONS)))
          .addMethod(
            getListOrganizationEnvIdsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest,
                choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse>(
                  this, METHODID_LIST_ORGANIZATION_ENV_IDS)))
          .addMethod(
            getCreateDefaultEnvironmentsForOrgsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest,
                choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse>(
                  this, METHODID_CREATE_DEFAULT_ENVIRONMENTS_FOR_ORGS)))
          .addMethod(
            getCreateGroupMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.CreateGroupRequest,
                choreo.apis.UserServiceOuterClass.CreateGroupResponse>(
                  this, METHODID_CREATE_GROUP)))
          .addMethod(
            getUpdateGroupMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.UpdateGroupRequest,
                choreo.apis.UserServiceOuterClass.UpdateGroupResponse>(
                  this, METHODID_UPDATE_GROUP)))
          .addMethod(
            getListGroupsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.ListGroupsRequest,
                choreo.apis.UserServiceOuterClass.ListGroupsResponse>(
                  this, METHODID_LIST_GROUPS)))
          .addMethod(
            getListUserGroupsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.ListUserGroupsRequest,
                choreo.apis.UserServiceOuterClass.ListUserGroupsResponse>(
                  this, METHODID_LIST_USER_GROUPS)))
          .addMethod(
            getGetGroupInfoMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.GetGroupInfoRequest,
                choreo.apis.UserServiceOuterClass.GetGroupInfoResponse>(
                  this, METHODID_GET_GROUP_INFO)))
          .addMethod(
            getDeleteGroupMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.DeleteGroupRequest,
                choreo.apis.UserServiceOuterClass.DeleteGroupResponse>(
                  this, METHODID_DELETE_GROUP)))
          .addMethod(
            getRemoveMemberFromGroupMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest,
                choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse>(
                  this, METHODID_REMOVE_MEMBER_FROM_GROUP)))
          .addMethod(
            getCreateRoleMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.CreateRoleRequest,
                choreo.apis.UserServiceOuterClass.CreateRoleResponse>(
                  this, METHODID_CREATE_ROLE)))
          .addMethod(
            getUpdateRoleMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.UpdateRoleRequest,
                choreo.apis.UserServiceOuterClass.UpdateRoleResponse>(
                  this, METHODID_UPDATE_ROLE)))
          .addMethod(
            getGetRoleInfoMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.GetRoleInfoRequest,
                choreo.apis.UserServiceOuterClass.GetRoleInfoResponse>(
                  this, METHODID_GET_ROLE_INFO)))
          .addMethod(
            getDeleteRoleMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.DeleteRoleRequest,
                choreo.apis.UserServiceOuterClass.DeleteRoleResponse>(
                  this, METHODID_DELETE_ROLE)))
          .addMethod(
            getListRolesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.ListRolesRequest,
                choreo.apis.UserServiceOuterClass.ListRolesResponse>(
                  this, METHODID_LIST_ROLES)))
          .addMethod(
            getGetPermissionListMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.GetPermissionListRequest,
                choreo.apis.UserServiceOuterClass.GetPermissionListResponse>(
                  this, METHODID_GET_PERMISSION_LIST)))
          .addMethod(
            getSendMemberInvitationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest,
                choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse>(
                  this, METHODID_SEND_MEMBER_INVITATION)))
          .addMethod(
            getAcceptMemberInvitationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest,
                choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse>(
                  this, METHODID_ACCEPT_MEMBER_INVITATION)))
          .addMethod(
            getFindMemberInvitationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest,
                choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse>(
                  this, METHODID_FIND_MEMBER_INVITATION)))
          .addMethod(
            getDeleteMemberInvitationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest,
                choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse>(
                  this, METHODID_DELETE_MEMBER_INVITATION)))
          .addMethod(
            getListMemberInvitationsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest,
                choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse>(
                  this, METHODID_LIST_MEMBER_INVITATIONS)))
          .build();
    }
  }

  /**
   */
  public static final class UserServiceStub extends io.grpc.stub.AbstractStub<UserServiceStub> {
    private UserServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserServiceStub(channel, callOptions);
    }

    /**
     */
    public void registerUser(choreo.apis.UserServiceOuterClass.RegisterUserRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.RegisterUserResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRegisterUserMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void validateUser(choreo.apis.UserServiceOuterClass.ValidateUserRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ValidateUserResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getValidateUserMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findUserFromIdpId(choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFindUserFromIdpIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addOrganizationUsers(choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAddOrganizationUsersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteOrganizationUser(choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteOrganizationUserMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findUserOrganizations(choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFindUserOrganizationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findUsersByOrganization(choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFindUsersByOrganizationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findUsersByOrganizationByRole(choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFindUsersByOrganizationByRoleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * todo: increase rpc payload size to 10mb
     * </pre>
     */
    public void createUserFeedback(choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateUserFeedbackMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUserInfo(choreo.apis.UserServiceOuterClass.GetUserInfoRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetUserInfoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetUserInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUserRoles(choreo.apis.UserServiceOuterClass.GetUserRolesRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetUserRolesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetUserRolesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getEnterpriseUserRoles(choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetUserRolesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetEnterpriseUserRolesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateMembersOfRole(choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateMembersOfRoleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateRolesOfMember(choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateRolesOfMemberMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void startInvitationFlow(choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getStartInvitationFlowMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Organizations
     * </pre>
     */
    public void createOrganization(choreo.apis.UserServiceOuterClass.CreateOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateOrganizationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateOrganizationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getOrganization(choreo.apis.UserServiceOuterClass.GetOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetOrganizationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetOrganizationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateOrganization(choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateOrganizationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteOrganization(choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteOrganizationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listOrganizations(choreo.apis.UserServiceOuterClass.ListOrganizationsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListOrganizationsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListOrganizationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listOrganizationEnvIds(choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListOrganizationEnvIdsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createDefaultEnvironmentsForOrgs(choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateDefaultEnvironmentsForOrgsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Groups
     * </pre>
     */
    public void createGroup(choreo.apis.UserServiceOuterClass.CreateGroupRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateGroupResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateGroupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateGroup(choreo.apis.UserServiceOuterClass.UpdateGroupRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateGroupResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateGroupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listGroups(choreo.apis.UserServiceOuterClass.ListGroupsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListGroupsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListGroupsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listUserGroups(choreo.apis.UserServiceOuterClass.ListUserGroupsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListUserGroupsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListUserGroupsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getGroupInfo(choreo.apis.UserServiceOuterClass.GetGroupInfoRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetGroupInfoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetGroupInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteGroup(choreo.apis.UserServiceOuterClass.DeleteGroupRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteGroupResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteGroupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void removeMemberFromGroup(choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRemoveMemberFromGroupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Roles
     * </pre>
     */
    public void createRole(choreo.apis.UserServiceOuterClass.CreateRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateRoleResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateRoleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateRole(choreo.apis.UserServiceOuterClass.UpdateRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateRoleResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateRoleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getRoleInfo(choreo.apis.UserServiceOuterClass.GetRoleInfoRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetRoleInfoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetRoleInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteRole(choreo.apis.UserServiceOuterClass.DeleteRoleRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteRoleResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteRoleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listRoles(choreo.apis.UserServiceOuterClass.ListRolesRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListRolesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListRolesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getPermissionList(choreo.apis.UserServiceOuterClass.GetPermissionListRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetPermissionListResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetPermissionListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Org-member invitations
     * </pre>
     */
    public void sendMemberInvitation(choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSendMemberInvitationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void acceptMemberInvitation(choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAcceptMemberInvitationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findMemberInvitation(choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFindMemberInvitationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteMemberInvitation(choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteMemberInvitationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listMemberInvitations(choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest request,
        io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListMemberInvitationsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class UserServiceBlockingStub extends io.grpc.stub.AbstractStub<UserServiceBlockingStub> {
    private UserServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.RegisterUserResponse registerUser(choreo.apis.UserServiceOuterClass.RegisterUserRequest request) {
      return blockingUnaryCall(
          getChannel(), getRegisterUserMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.ValidateUserResponse validateUser(choreo.apis.UserServiceOuterClass.ValidateUserRequest request) {
      return blockingUnaryCall(
          getChannel(), getValidateUserMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse findUserFromIdpId(choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest request) {
      return blockingUnaryCall(
          getChannel(), getFindUserFromIdpIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty addOrganizationUsers(choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest request) {
      return blockingUnaryCall(
          getChannel(), getAddOrganizationUsersMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty deleteOrganizationUser(choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest request) {
      return blockingUnaryCall(
          getChannel(), getDeleteOrganizationUserMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse findUserOrganizations(choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest request) {
      return blockingUnaryCall(
          getChannel(), getFindUserOrganizationsMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse findUsersByOrganization(choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest request) {
      return blockingUnaryCall(
          getChannel(), getFindUsersByOrganizationMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse findUsersByOrganizationByRole(choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest request) {
      return blockingUnaryCall(
          getChannel(), getFindUsersByOrganizationByRoleMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * todo: increase rpc payload size to 10mb
     * </pre>
     */
    public choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse createUserFeedback(choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest request) {
      return blockingUnaryCall(
          getChannel(), getCreateUserFeedbackMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.GetUserInfoResponse getUserInfo(choreo.apis.UserServiceOuterClass.GetUserInfoRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetUserInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.GetUserRolesResponse getUserRoles(choreo.apis.UserServiceOuterClass.GetUserRolesRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetUserRolesMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.GetUserRolesResponse getEnterpriseUserRoles(choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetEnterpriseUserRolesMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse updateMembersOfRole(choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest request) {
      return blockingUnaryCall(
          getChannel(), getUpdateMembersOfRoleMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse updateRolesOfMember(choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest request) {
      return blockingUnaryCall(
          getChannel(), getUpdateRolesOfMemberMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse startInvitationFlow(choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest request) {
      return blockingUnaryCall(
          getChannel(), getStartInvitationFlowMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Organizations
     * </pre>
     */
    public choreo.apis.UserServiceOuterClass.CreateOrganizationResponse createOrganization(choreo.apis.UserServiceOuterClass.CreateOrganizationRequest request) {
      return blockingUnaryCall(
          getChannel(), getCreateOrganizationMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.GetOrganizationResponse getOrganization(choreo.apis.UserServiceOuterClass.GetOrganizationRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetOrganizationMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse updateOrganization(choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest request) {
      return blockingUnaryCall(
          getChannel(), getUpdateOrganizationMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse deleteOrganization(choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest request) {
      return blockingUnaryCall(
          getChannel(), getDeleteOrganizationMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.ListOrganizationsResponse listOrganizations(choreo.apis.UserServiceOuterClass.ListOrganizationsRequest request) {
      return blockingUnaryCall(
          getChannel(), getListOrganizationsMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse listOrganizationEnvIds(choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest request) {
      return blockingUnaryCall(
          getChannel(), getListOrganizationEnvIdsMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse createDefaultEnvironmentsForOrgs(choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest request) {
      return blockingUnaryCall(
          getChannel(), getCreateDefaultEnvironmentsForOrgsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Groups
     * </pre>
     */
    public choreo.apis.UserServiceOuterClass.CreateGroupResponse createGroup(choreo.apis.UserServiceOuterClass.CreateGroupRequest request) {
      return blockingUnaryCall(
          getChannel(), getCreateGroupMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.UpdateGroupResponse updateGroup(choreo.apis.UserServiceOuterClass.UpdateGroupRequest request) {
      return blockingUnaryCall(
          getChannel(), getUpdateGroupMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.ListGroupsResponse listGroups(choreo.apis.UserServiceOuterClass.ListGroupsRequest request) {
      return blockingUnaryCall(
          getChannel(), getListGroupsMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.ListUserGroupsResponse listUserGroups(choreo.apis.UserServiceOuterClass.ListUserGroupsRequest request) {
      return blockingUnaryCall(
          getChannel(), getListUserGroupsMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.GetGroupInfoResponse getGroupInfo(choreo.apis.UserServiceOuterClass.GetGroupInfoRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetGroupInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.DeleteGroupResponse deleteGroup(choreo.apis.UserServiceOuterClass.DeleteGroupRequest request) {
      return blockingUnaryCall(
          getChannel(), getDeleteGroupMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse removeMemberFromGroup(choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest request) {
      return blockingUnaryCall(
          getChannel(), getRemoveMemberFromGroupMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Roles
     * </pre>
     */
    public choreo.apis.UserServiceOuterClass.CreateRoleResponse createRole(choreo.apis.UserServiceOuterClass.CreateRoleRequest request) {
      return blockingUnaryCall(
          getChannel(), getCreateRoleMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.UpdateRoleResponse updateRole(choreo.apis.UserServiceOuterClass.UpdateRoleRequest request) {
      return blockingUnaryCall(
          getChannel(), getUpdateRoleMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.GetRoleInfoResponse getRoleInfo(choreo.apis.UserServiceOuterClass.GetRoleInfoRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetRoleInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.DeleteRoleResponse deleteRole(choreo.apis.UserServiceOuterClass.DeleteRoleRequest request) {
      return blockingUnaryCall(
          getChannel(), getDeleteRoleMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.ListRolesResponse listRoles(choreo.apis.UserServiceOuterClass.ListRolesRequest request) {
      return blockingUnaryCall(
          getChannel(), getListRolesMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.GetPermissionListResponse getPermissionList(choreo.apis.UserServiceOuterClass.GetPermissionListRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetPermissionListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Org-member invitations
     * </pre>
     */
    public choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse sendMemberInvitation(choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest request) {
      return blockingUnaryCall(
          getChannel(), getSendMemberInvitationMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse acceptMemberInvitation(choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest request) {
      return blockingUnaryCall(
          getChannel(), getAcceptMemberInvitationMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse findMemberInvitation(choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest request) {
      return blockingUnaryCall(
          getChannel(), getFindMemberInvitationMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse deleteMemberInvitation(choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest request) {
      return blockingUnaryCall(
          getChannel(), getDeleteMemberInvitationMethod(), getCallOptions(), request);
    }

    /**
     */
    public choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse listMemberInvitations(choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest request) {
      return blockingUnaryCall(
          getChannel(), getListMemberInvitationsMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class UserServiceFutureStub extends io.grpc.stub.AbstractStub<UserServiceFutureStub> {
    private UserServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.RegisterUserResponse> registerUser(
        choreo.apis.UserServiceOuterClass.RegisterUserRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRegisterUserMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.ValidateUserResponse> validateUser(
        choreo.apis.UserServiceOuterClass.ValidateUserRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getValidateUserMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse> findUserFromIdpId(
        choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getFindUserFromIdpIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> addOrganizationUsers(
        choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAddOrganizationUsersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteOrganizationUser(
        choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteOrganizationUserMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse> findUserOrganizations(
        choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getFindUserOrganizationsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse> findUsersByOrganization(
        choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getFindUsersByOrganizationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse> findUsersByOrganizationByRole(
        choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getFindUsersByOrganizationByRoleMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * todo: increase rpc payload size to 10mb
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse> createUserFeedback(
        choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateUserFeedbackMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.GetUserInfoResponse> getUserInfo(
        choreo.apis.UserServiceOuterClass.GetUserInfoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetUserInfoMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.GetUserRolesResponse> getUserRoles(
        choreo.apis.UserServiceOuterClass.GetUserRolesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetUserRolesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.GetUserRolesResponse> getEnterpriseUserRoles(
        choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetEnterpriseUserRolesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse> updateMembersOfRole(
        choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateMembersOfRoleMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse> updateRolesOfMember(
        choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateRolesOfMemberMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse> startInvitationFlow(
        choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getStartInvitationFlowMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Organizations
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.CreateOrganizationResponse> createOrganization(
        choreo.apis.UserServiceOuterClass.CreateOrganizationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateOrganizationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.GetOrganizationResponse> getOrganization(
        choreo.apis.UserServiceOuterClass.GetOrganizationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetOrganizationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse> updateOrganization(
        choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateOrganizationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse> deleteOrganization(
        choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteOrganizationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.ListOrganizationsResponse> listOrganizations(
        choreo.apis.UserServiceOuterClass.ListOrganizationsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListOrganizationsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse> listOrganizationEnvIds(
        choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListOrganizationEnvIdsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse> createDefaultEnvironmentsForOrgs(
        choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateDefaultEnvironmentsForOrgsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Groups
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.CreateGroupResponse> createGroup(
        choreo.apis.UserServiceOuterClass.CreateGroupRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateGroupMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.UpdateGroupResponse> updateGroup(
        choreo.apis.UserServiceOuterClass.UpdateGroupRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateGroupMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.ListGroupsResponse> listGroups(
        choreo.apis.UserServiceOuterClass.ListGroupsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListGroupsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.ListUserGroupsResponse> listUserGroups(
        choreo.apis.UserServiceOuterClass.ListUserGroupsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListUserGroupsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.GetGroupInfoResponse> getGroupInfo(
        choreo.apis.UserServiceOuterClass.GetGroupInfoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetGroupInfoMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.DeleteGroupResponse> deleteGroup(
        choreo.apis.UserServiceOuterClass.DeleteGroupRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteGroupMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse> removeMemberFromGroup(
        choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRemoveMemberFromGroupMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Roles
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.CreateRoleResponse> createRole(
        choreo.apis.UserServiceOuterClass.CreateRoleRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateRoleMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.UpdateRoleResponse> updateRole(
        choreo.apis.UserServiceOuterClass.UpdateRoleRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateRoleMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.GetRoleInfoResponse> getRoleInfo(
        choreo.apis.UserServiceOuterClass.GetRoleInfoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetRoleInfoMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.DeleteRoleResponse> deleteRole(
        choreo.apis.UserServiceOuterClass.DeleteRoleRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteRoleMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.ListRolesResponse> listRoles(
        choreo.apis.UserServiceOuterClass.ListRolesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListRolesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.GetPermissionListResponse> getPermissionList(
        choreo.apis.UserServiceOuterClass.GetPermissionListRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetPermissionListMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Org-member invitations
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse> sendMemberInvitation(
        choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSendMemberInvitationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse> acceptMemberInvitation(
        choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAcceptMemberInvitationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse> findMemberInvitation(
        choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getFindMemberInvitationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse> deleteMemberInvitation(
        choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteMemberInvitationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse> listMemberInvitations(
        choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListMemberInvitationsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REGISTER_USER = 0;
  private static final int METHODID_VALIDATE_USER = 1;
  private static final int METHODID_FIND_USER_FROM_IDP_ID = 2;
  private static final int METHODID_ADD_ORGANIZATION_USERS = 3;
  private static final int METHODID_DELETE_ORGANIZATION_USER = 4;
  private static final int METHODID_FIND_USER_ORGANIZATIONS = 5;
  private static final int METHODID_FIND_USERS_BY_ORGANIZATION = 6;
  private static final int METHODID_FIND_USERS_BY_ORGANIZATION_BY_ROLE = 7;
  private static final int METHODID_CREATE_USER_FEEDBACK = 8;
  private static final int METHODID_GET_USER_INFO = 9;
  private static final int METHODID_GET_USER_ROLES = 10;
  private static final int METHODID_GET_ENTERPRISE_USER_ROLES = 11;
  private static final int METHODID_UPDATE_MEMBERS_OF_ROLE = 12;
  private static final int METHODID_UPDATE_ROLES_OF_MEMBER = 13;
  private static final int METHODID_START_INVITATION_FLOW = 14;
  private static final int METHODID_CREATE_ORGANIZATION = 15;
  private static final int METHODID_GET_ORGANIZATION = 16;
  private static final int METHODID_UPDATE_ORGANIZATION = 17;
  private static final int METHODID_DELETE_ORGANIZATION = 18;
  private static final int METHODID_LIST_ORGANIZATIONS = 19;
  private static final int METHODID_LIST_ORGANIZATION_ENV_IDS = 20;
  private static final int METHODID_CREATE_DEFAULT_ENVIRONMENTS_FOR_ORGS = 21;
  private static final int METHODID_CREATE_GROUP = 22;
  private static final int METHODID_UPDATE_GROUP = 23;
  private static final int METHODID_LIST_GROUPS = 24;
  private static final int METHODID_LIST_USER_GROUPS = 25;
  private static final int METHODID_GET_GROUP_INFO = 26;
  private static final int METHODID_DELETE_GROUP = 27;
  private static final int METHODID_REMOVE_MEMBER_FROM_GROUP = 28;
  private static final int METHODID_CREATE_ROLE = 29;
  private static final int METHODID_UPDATE_ROLE = 30;
  private static final int METHODID_GET_ROLE_INFO = 31;
  private static final int METHODID_DELETE_ROLE = 32;
  private static final int METHODID_LIST_ROLES = 33;
  private static final int METHODID_GET_PERMISSION_LIST = 34;
  private static final int METHODID_SEND_MEMBER_INVITATION = 35;
  private static final int METHODID_ACCEPT_MEMBER_INVITATION = 36;
  private static final int METHODID_FIND_MEMBER_INVITATION = 37;
  private static final int METHODID_DELETE_MEMBER_INVITATION = 38;
  private static final int METHODID_LIST_MEMBER_INVITATIONS = 39;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final UserServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(UserServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REGISTER_USER:
          serviceImpl.registerUser((choreo.apis.UserServiceOuterClass.RegisterUserRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.RegisterUserResponse>) responseObserver);
          break;
        case METHODID_VALIDATE_USER:
          serviceImpl.validateUser((choreo.apis.UserServiceOuterClass.ValidateUserRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ValidateUserResponse>) responseObserver);
          break;
        case METHODID_FIND_USER_FROM_IDP_ID:
          serviceImpl.findUserFromIdpId((choreo.apis.UserServiceOuterClass.FindUserFromIdpIdRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUserFromIdpIdResponse>) responseObserver);
          break;
        case METHODID_ADD_ORGANIZATION_USERS:
          serviceImpl.addOrganizationUsers((choreo.apis.UserServiceOuterClass.AddOrganizationUsersRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_ORGANIZATION_USER:
          serviceImpl.deleteOrganizationUser((choreo.apis.UserServiceOuterClass.DeleteOrganizationUserRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_FIND_USER_ORGANIZATIONS:
          serviceImpl.findUserOrganizations((choreo.apis.UserServiceOuterClass.FindUserOrganizationsRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUserOrganizationsResponse>) responseObserver);
          break;
        case METHODID_FIND_USERS_BY_ORGANIZATION:
          serviceImpl.findUsersByOrganization((choreo.apis.UserServiceOuterClass.FindUsersByOrganizationRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationResponse>) responseObserver);
          break;
        case METHODID_FIND_USERS_BY_ORGANIZATION_BY_ROLE:
          serviceImpl.findUsersByOrganizationByRole((choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindUsersByOrganizationByRoleResponse>) responseObserver);
          break;
        case METHODID_CREATE_USER_FEEDBACK:
          serviceImpl.createUserFeedback((choreo.apis.UserServiceOuterClass.CreateUserFeedbackRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateUserFeedbackResponse>) responseObserver);
          break;
        case METHODID_GET_USER_INFO:
          serviceImpl.getUserInfo((choreo.apis.UserServiceOuterClass.GetUserInfoRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetUserInfoResponse>) responseObserver);
          break;
        case METHODID_GET_USER_ROLES:
          serviceImpl.getUserRoles((choreo.apis.UserServiceOuterClass.GetUserRolesRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetUserRolesResponse>) responseObserver);
          break;
        case METHODID_GET_ENTERPRISE_USER_ROLES:
          serviceImpl.getEnterpriseUserRoles((choreo.apis.UserServiceOuterClass.GetEnterpriseUserRolesRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetUserRolesResponse>) responseObserver);
          break;
        case METHODID_UPDATE_MEMBERS_OF_ROLE:
          serviceImpl.updateMembersOfRole((choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateMembersOfRoleResponse>) responseObserver);
          break;
        case METHODID_UPDATE_ROLES_OF_MEMBER:
          serviceImpl.updateRolesOfMember((choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateRolesOfMemberResponse>) responseObserver);
          break;
        case METHODID_START_INVITATION_FLOW:
          serviceImpl.startInvitationFlow((choreo.apis.UserServiceOuterClass.StartInvitationFlowRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.StartInvitationFlowResponse>) responseObserver);
          break;
        case METHODID_CREATE_ORGANIZATION:
          serviceImpl.createOrganization((choreo.apis.UserServiceOuterClass.CreateOrganizationRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateOrganizationResponse>) responseObserver);
          break;
        case METHODID_GET_ORGANIZATION:
          serviceImpl.getOrganization((choreo.apis.UserServiceOuterClass.GetOrganizationRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetOrganizationResponse>) responseObserver);
          break;
        case METHODID_UPDATE_ORGANIZATION:
          serviceImpl.updateOrganization((choreo.apis.UserServiceOuterClass.UpdateOrganizationRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateOrganizationResponse>) responseObserver);
          break;
        case METHODID_DELETE_ORGANIZATION:
          serviceImpl.deleteOrganization((choreo.apis.UserServiceOuterClass.DeleteOrganizationRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteOrganizationResponse>) responseObserver);
          break;
        case METHODID_LIST_ORGANIZATIONS:
          serviceImpl.listOrganizations((choreo.apis.UserServiceOuterClass.ListOrganizationsRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListOrganizationsResponse>) responseObserver);
          break;
        case METHODID_LIST_ORGANIZATION_ENV_IDS:
          serviceImpl.listOrganizationEnvIds((choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListOrganizationEnvIdsResponse>) responseObserver);
          break;
        case METHODID_CREATE_DEFAULT_ENVIRONMENTS_FOR_ORGS:
          serviceImpl.createDefaultEnvironmentsForOrgs((choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateDefaultEnvironmentsForOrgsResponse>) responseObserver);
          break;
        case METHODID_CREATE_GROUP:
          serviceImpl.createGroup((choreo.apis.UserServiceOuterClass.CreateGroupRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateGroupResponse>) responseObserver);
          break;
        case METHODID_UPDATE_GROUP:
          serviceImpl.updateGroup((choreo.apis.UserServiceOuterClass.UpdateGroupRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateGroupResponse>) responseObserver);
          break;
        case METHODID_LIST_GROUPS:
          serviceImpl.listGroups((choreo.apis.UserServiceOuterClass.ListGroupsRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListGroupsResponse>) responseObserver);
          break;
        case METHODID_LIST_USER_GROUPS:
          serviceImpl.listUserGroups((choreo.apis.UserServiceOuterClass.ListUserGroupsRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListUserGroupsResponse>) responseObserver);
          break;
        case METHODID_GET_GROUP_INFO:
          serviceImpl.getGroupInfo((choreo.apis.UserServiceOuterClass.GetGroupInfoRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetGroupInfoResponse>) responseObserver);
          break;
        case METHODID_DELETE_GROUP:
          serviceImpl.deleteGroup((choreo.apis.UserServiceOuterClass.DeleteGroupRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteGroupResponse>) responseObserver);
          break;
        case METHODID_REMOVE_MEMBER_FROM_GROUP:
          serviceImpl.removeMemberFromGroup((choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.RemoveMemberFromGroupResponse>) responseObserver);
          break;
        case METHODID_CREATE_ROLE:
          serviceImpl.createRole((choreo.apis.UserServiceOuterClass.CreateRoleRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.CreateRoleResponse>) responseObserver);
          break;
        case METHODID_UPDATE_ROLE:
          serviceImpl.updateRole((choreo.apis.UserServiceOuterClass.UpdateRoleRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.UpdateRoleResponse>) responseObserver);
          break;
        case METHODID_GET_ROLE_INFO:
          serviceImpl.getRoleInfo((choreo.apis.UserServiceOuterClass.GetRoleInfoRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetRoleInfoResponse>) responseObserver);
          break;
        case METHODID_DELETE_ROLE:
          serviceImpl.deleteRole((choreo.apis.UserServiceOuterClass.DeleteRoleRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteRoleResponse>) responseObserver);
          break;
        case METHODID_LIST_ROLES:
          serviceImpl.listRoles((choreo.apis.UserServiceOuterClass.ListRolesRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListRolesResponse>) responseObserver);
          break;
        case METHODID_GET_PERMISSION_LIST:
          serviceImpl.getPermissionList((choreo.apis.UserServiceOuterClass.GetPermissionListRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.GetPermissionListResponse>) responseObserver);
          break;
        case METHODID_SEND_MEMBER_INVITATION:
          serviceImpl.sendMemberInvitation((choreo.apis.UserServiceOuterClass.SendMemberInvitationRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.SendMemberInvitationResponse>) responseObserver);
          break;
        case METHODID_ACCEPT_MEMBER_INVITATION:
          serviceImpl.acceptMemberInvitation((choreo.apis.UserServiceOuterClass.AcceptMemberInvitationRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.AcceptMemberInvitationResponse>) responseObserver);
          break;
        case METHODID_FIND_MEMBER_INVITATION:
          serviceImpl.findMemberInvitation((choreo.apis.UserServiceOuterClass.FindMemberInvitationRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.FindMemberInvitationResponse>) responseObserver);
          break;
        case METHODID_DELETE_MEMBER_INVITATION:
          serviceImpl.deleteMemberInvitation((choreo.apis.UserServiceOuterClass.DeleteMemberInvitationRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.DeleteMemberInvitationResponse>) responseObserver);
          break;
        case METHODID_LIST_MEMBER_INVITATIONS:
          serviceImpl.listMemberInvitations((choreo.apis.UserServiceOuterClass.ListMemberInvitationsRequest) request,
              (io.grpc.stub.StreamObserver<choreo.apis.UserServiceOuterClass.ListMemberInvitationsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class UserServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UserServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return choreo.apis.UserServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UserService");
    }
  }

  private static final class UserServiceFileDescriptorSupplier
      extends UserServiceBaseDescriptorSupplier {
    UserServiceFileDescriptorSupplier() {}
  }

  private static final class UserServiceMethodDescriptorSupplier
      extends UserServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    UserServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UserServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UserServiceFileDescriptorSupplier())
              .addMethod(getRegisterUserMethod())
              .addMethod(getValidateUserMethod())
              .addMethod(getFindUserFromIdpIdMethod())
              .addMethod(getAddOrganizationUsersMethod())
              .addMethod(getDeleteOrganizationUserMethod())
              .addMethod(getFindUserOrganizationsMethod())
              .addMethod(getFindUsersByOrganizationMethod())
              .addMethod(getFindUsersByOrganizationByRoleMethod())
              .addMethod(getCreateUserFeedbackMethod())
              .addMethod(getGetUserInfoMethod())
              .addMethod(getGetUserRolesMethod())
              .addMethod(getGetEnterpriseUserRolesMethod())
              .addMethod(getUpdateMembersOfRoleMethod())
              .addMethod(getUpdateRolesOfMemberMethod())
              .addMethod(getStartInvitationFlowMethod())
              .addMethod(getCreateOrganizationMethod())
              .addMethod(getGetOrganizationMethod())
              .addMethod(getUpdateOrganizationMethod())
              .addMethod(getDeleteOrganizationMethod())
              .addMethod(getListOrganizationsMethod())
              .addMethod(getListOrganizationEnvIdsMethod())
              .addMethod(getCreateDefaultEnvironmentsForOrgsMethod())
              .addMethod(getCreateGroupMethod())
              .addMethod(getUpdateGroupMethod())
              .addMethod(getListGroupsMethod())
              .addMethod(getListUserGroupsMethod())
              .addMethod(getGetGroupInfoMethod())
              .addMethod(getDeleteGroupMethod())
              .addMethod(getRemoveMemberFromGroupMethod())
              .addMethod(getCreateRoleMethod())
              .addMethod(getUpdateRoleMethod())
              .addMethod(getGetRoleInfoMethod())
              .addMethod(getDeleteRoleMethod())
              .addMethod(getListRolesMethod())
              .addMethod(getGetPermissionListMethod())
              .addMethod(getSendMemberInvitationMethod())
              .addMethod(getAcceptMemberInvitationMethod())
              .addMethod(getFindMemberInvitationMethod())
              .addMethod(getDeleteMemberInvitationMethod())
              .addMethod(getListMemberInvitationsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
