package com.example.userservice.grpc;

import com.example.userservice.grpc.proto.GetPlayerRequest;
import com.example.userservice.grpc.proto.PlayerReply;
import com.example.userservice.grpc.proto.UserGrpcServiceGrpc;
import com.example.userservice.repository.PlayerRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

/**
 * gRPC server endpoint. Game Service calls GetPlayer over gRPC (binary, fast,
 * internal) to resolve a player's display name and stats when rendering a game.
 *
 * @GrpcService registers this bean as a gRPC service with the Spring gRPC
 * autoconfigured server (port set via grpc.server.port in application.properties).
 *
 * The generated base class UserGrpcServiceGrpc.UserGrpcServiceImplBase comes
 * from src/main/proto/user.proto at build time.
 */
@GrpcService
public class UserGrpcServer extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    private final PlayerRepository repository;

    public UserGrpcServer(PlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void getPlayer(GetPlayerRequest request, StreamObserver<PlayerReply> responseObserver) {
        PlayerReply reply = repository.findById(request.getUserId())
                .map(p -> PlayerReply.newBuilder()
                        .setUserId(p.getId())
                        .setUsername(p.getUsername())
                        .setDisplayName(p.getDisplayName() == null ? p.getUsername() : p.getDisplayName())
                        .setWins(p.getWins())
                        .setLosses(p.getLosses())
                        .setFound(true)
                        .build())
                .orElseGet(() -> PlayerReply.newBuilder()
                        .setUserId(request.getUserId())
                        .setFound(false)
                        .build());

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
