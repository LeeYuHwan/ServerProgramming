package com.grpc.practice.demo.service;


import com.grpc.practice.proto.HelloRequest;
import com.grpc.practice.proto.HelloResponse;
import com.grpc.practice.proto.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class GrpcServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {
    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        String str = "Hello " + request.getFirstName() + request.getLastName();

        System.out.println(str);

        HelloResponse response = HelloResponse.newBuilder().setGreeting(str).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
