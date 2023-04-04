package org.wso2.carbon.apimgt.impl.authz.apis;

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
    comments = "Source: authorization_service.proto")
public final class AuthorizationGrpc {

  private AuthorizationGrpc() {}

  public static final String SERVICE_NAME = "org.wso2.carbon.apimgt.impl.authz.apis.Authorization";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest,
      org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse> getAuthorizeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "authorize",
      requestType = org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest.class,
      responseType = org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest,
      org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse> getAuthorizeMethod() {
    io.grpc.MethodDescriptor<org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest, org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse> getAuthorizeMethod;
    if ((getAuthorizeMethod = AuthorizationGrpc.getAuthorizeMethod) == null) {
      synchronized (AuthorizationGrpc.class) {
        if ((getAuthorizeMethod = AuthorizationGrpc.getAuthorizeMethod) == null) {
          AuthorizationGrpc.getAuthorizeMethod = getAuthorizeMethod = 
              io.grpc.MethodDescriptor.<org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest, org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "org.wso2.carbon.apimgt.impl.authz.apis.Authorization", "authorize"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new AuthorizationMethodDescriptorSupplier("authorize"))
                  .build();
          }
        }
     }
     return getAuthorizeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AuthorizationStub newStub(io.grpc.Channel channel) {
    return new AuthorizationStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AuthorizationBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new AuthorizationBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static AuthorizationFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new AuthorizationFutureStub(channel);
  }

  /**
   */
  public static abstract class AuthorizationImplBase implements io.grpc.BindableService {

    /**
     */
    public void authorize(org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest request,
        io.grpc.stub.StreamObserver<org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getAuthorizeMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getAuthorizeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest,
                org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse>(
                  this, METHODID_AUTHORIZE)))
          .build();
    }
  }

  /**
   */
  public static final class AuthorizationStub extends io.grpc.stub.AbstractStub<AuthorizationStub> {
    private AuthorizationStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AuthorizationStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthorizationStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AuthorizationStub(channel, callOptions);
    }

    /**
     */
    public void authorize(org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest request,
        io.grpc.stub.StreamObserver<org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAuthorizeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class AuthorizationBlockingStub extends io.grpc.stub.AbstractStub<AuthorizationBlockingStub> {
    private AuthorizationBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AuthorizationBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthorizationBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AuthorizationBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse authorize(org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest request) {
      return blockingUnaryCall(
          getChannel(), getAuthorizeMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class AuthorizationFutureStub extends io.grpc.stub.AbstractStub<AuthorizationFutureStub> {
    private AuthorizationFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AuthorizationFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthorizationFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AuthorizationFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse> authorize(
        org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAuthorizeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_AUTHORIZE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AuthorizationImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(AuthorizationImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_AUTHORIZE:
          serviceImpl.authorize((org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationRequest) request,
              (io.grpc.stub.StreamObserver<org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.AuthorizationResponse>) responseObserver);
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

  private static abstract class AuthorizationBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    AuthorizationBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.wso2.carbon.apimgt.impl.authz.apis.AuthorizationService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Authorization");
    }
  }

  private static final class AuthorizationFileDescriptorSupplier
      extends AuthorizationBaseDescriptorSupplier {
    AuthorizationFileDescriptorSupplier() {}
  }

  private static final class AuthorizationMethodDescriptorSupplier
      extends AuthorizationBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    AuthorizationMethodDescriptorSupplier(String methodName) {
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
      synchronized (AuthorizationGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AuthorizationFileDescriptorSupplier())
              .addMethod(getAuthorizeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
