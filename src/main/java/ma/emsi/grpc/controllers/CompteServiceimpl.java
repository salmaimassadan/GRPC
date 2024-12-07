package ma.emsi.grpc.controllers;

import io.grpc.stub.StreamObserver;
import ma.emsi.grpc.stubs.*;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
public class CompteServiceimpl extends CompteServiceGrpc.CompteServiceImplBase {

    private final Map<String, Compte> compteDB = new ConcurrentHashMap<>();

    public CompteServiceimpl() {
        compteDB.put("1", Compte.newBuilder().setId("1").setSolde(1000.0f).setDateCreation("2024-12-01").setType(TypeCompte.COURANT).build());
        compteDB.put("2", Compte.newBuilder().setId("2").setSolde(500.0f).setDateCreation("2024-12-02").setType(TypeCompte.EPARGNE).build());
        compteDB.put("3", Compte.newBuilder().setId("3").setSolde(1500.0f).setDateCreation("2024-12-03").setType(TypeCompte.COURANT).build());
    }

    @Override
    public void allComptes(GetAllComptesRequest request, StreamObserver<GetAllComptesResponse> responseObserver) {
        GetAllComptesResponse response = GetAllComptesResponse.newBuilder()
                .addAllComptes(compteDB.values())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void compteById(GetCompteByIdRequest request, StreamObserver<GetCompteByIdResponse> responseObserver) {
        Compte compte = compteDB.get(request.getId());
        if (compte != null) {
            responseObserver.onNext(GetCompteByIdResponse.newBuilder().setCompte(compte).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription("Compte avec l'ID " + request.getId() + " non trouvé")
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void totalSolde(GetTotalSoldeRequest request, StreamObserver<GetTotalSoldeResponse> responseObserver) {
        int count = compteDB.size();
        float sum = compteDB.values().stream().map(Compte::getSolde).reduce(0f, Float::sum);
        float average = count > 0 ? sum / count : 0;

        SoldeStats stats = SoldeStats.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAverage(average)
                .build();

        responseObserver.onNext(GetTotalSoldeResponse.newBuilder().setStats(stats).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getComptesByType(GetComptesByTypeRequest request, StreamObserver<GetComptesByTypeResponse> responseObserver) {
        TypeCompte requestedType = request.getType();
        GetComptesByTypeResponse response = GetComptesByTypeResponse.newBuilder()
                .addAllComptes(compteDB.values().stream()
                        .filter(compte -> compte.getType() == requestedType)
                        .toList())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteCompte(DeleteCompteRequest request, StreamObserver<DeleteCompteResponse> responseObserver) {
        String id = request.getId();
        if (compteDB.containsKey(id)) {
            compteDB.remove(id);
            DeleteCompteResponse response = DeleteCompteResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription("Compte avec l'ID " + id + " non trouvé.")
                            .asRuntimeException()
            );
        }
    }
}
