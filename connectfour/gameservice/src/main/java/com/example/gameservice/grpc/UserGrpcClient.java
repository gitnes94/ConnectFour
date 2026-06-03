package com.example.gameservice.grpc;

import com.example.userservice.grpc.proto.GetPlayerRequest;
import com.example.userservice.grpc.proto.PlayerReply;
import com.example.userservice.grpc.proto.UserGrpcServiceGrpc;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

/**
 * gRPC CLIENT side. Calls User Service's GetPlayer over gRPC to resolve a
 * player's display name. The channel target "userservice" is configured in
 * application.properties under spring.grpc.client.channels.userservice.
 *
 * This is the "intern kommunikation via gRPC" requirement, from the caller's side.
 */
@Component
public class UserGrpcClient {

    private final UserGrpcServiceGrpc.UserGrpcServiceBlockingStub stub;

    public UserGrpcClient(GrpcChannelFactory channels) {
        var channel = channels.createChannel("userservice");
        this.stub = UserGrpcServiceGrpc.newBlockingStub(channel);
    }

    public String resolveDisplayName(String userId) {
        try {
            PlayerReply reply = stub.getPlayer(
                    GetPlayerRequest.newBuilder().setUserId(userId).build());
            if (reply.getFound()) {
                return reply.getDisplayName();
            }
        } catch (Exception ex) {
            // gRPC failure should not break gameplay; fall back to the raw id.
        }
        return userId;
    }
}
