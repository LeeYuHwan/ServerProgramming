package com.grpc.practice.client.demo;

import com.grpc.practice.client.proto.HelloRequest;
import com.grpc.practice.client.proto.HelloResponse;
import com.grpc.practice.client.proto.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CallTest {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();

        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);

        HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder().setFirstName("doha").setLastName("lee").build());

        System.out.println(helloResponse.getGreeting());

        channel.shutdown();

    }
}
