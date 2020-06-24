package gray.light.handler;

import gray.light.document.entity.Document;
import gray.light.document.entity.DocumentCatalog;
import gray.light.document.entity.DocumentChapter;
import gray.light.document.customizer.DocumentCustomizer;
import gray.light.owner.customizer.OwnerProjectCustomizer;
import gray.light.owner.entity.Owner;
import gray.light.owner.entity.OwnerProject;
import gray.light.document.service.DocumentRelationService;
import gray.light.owner.service.OverallOwnerService;
import gray.light.owner.service.SuperOwnerService;
import gray.light.business.CatalogsTreeWalker;
import gray.light.business.ContainsCatalogCatalogBo;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import perishing.constraint.jdbc.Page;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static gray.light.web.ResponseToClient.*;
import static gray.light.web.ResponseToClient.allRightFromValue;
import static gray.light.web.UnsatisfiedBodyExtractors.*;

/**
 * 此handler提供与个人相关的操作，例如项目、文档、博客等等
 *
 * @author XyParaCrim
 */
@CommonsLog
@Component
@RequiredArgsConstructor
public class OwnerFavoritesHandler {

    private final SuperOwnerService superOwnerService;

    private final OverallOwnerService overallOwnerService;

    private final DocumentRelationService documentRelationService;

    /**
     * 为超级所属者添加一个项目
     *
     * @param request 服务请求
     * @return Response of Publisher
     */
    public Mono<ServerResponse> addFavoriteProject(ServerRequest request) {
        return request.
                bodyToMono(Map.class).
                flatMap(
                        body -> unsatisfiedChain().
                                chain("name", EXTRACT_NAME).
                                chain("description", EXTRACT_DEFAULT).
                                extractOrOther(body, () -> {
                                    OwnerProject ownerProject = OwnerProjectCustomizer.uncheck(body.get("name"), body.get("description"));

                                    return Mono.fromFuture(CompletableFuture.supplyAsync(() -> overallOwnerService.addProject(superOwnerService.superOwner(), ownerProject)))
                                            .flatMap(isAdded -> isAdded ?
                                                    allRightFromValue(ownerProject) :
                                                    failWithMessage("Failed to add favorite project."));
                                })
                );
    }

    /**
     * 为超级所属者的项目添加一个文档
     *
     * @param request 服务请求
     * @return Response of Publisher
     */
    public Mono<ServerResponse> addFavoriteDocument(ServerRequest request) {
        return request.
                bodyToMono(Map.class).
                flatMap(
                        body -> unsatisfiedChain().
                                chain("title", EXTRACT_NAME).
                                chain("description", EXTRACT_DEFAULT).
                                chain("projectId", EXTRACT_LONG).
                                chain("repoUrl", EXTRACT_GIT).
                                extractOrOther(body, () -> {
                                    Document document = DocumentCustomizer.necessary(body);

                                    return Mono.fromFuture(CompletableFuture.supplyAsync(() -> documentRelationService.createDocumentForProject(document)))
                                            .flatMap(success -> success ?
                                                    allRightFromValue(document) :
                                                    failWithMessage("Failed to add favorite project.")
                                            );
                                })
                );
    }

    /**
     * 查询超级所属者的所有文档
     *
     * @param request 服务请求
     * @return Response of Publisher
     */
    public Mono<ServerResponse> queryFavoriteDocument(ServerRequest request) {
        Optional<String> pages = request.queryParam("pages");
        Optional<String> count = request.queryParam("count");
        Owner superOwner = superOwnerService.superOwner();
        Page page = pages.isPresent() && count.isPresent() ? Page.newPage(pages.get(), count.get()) : Page.unlimited();

        return allRightFromValue(overallOwnerService.documentProjects(superOwner, page));
    }

    /**
     * 查询文档仓库的结构树
     *
     * @param request 服务请求
     * @return Response of Publisher
     */
    public Mono<ServerResponse> queryFavoriteDocumentRepositoryTree(ServerRequest request) {
        Optional<String> idOp = request.queryParam("id");

        if (idOp.isEmpty()) {
            return failWithMessage("Illegal parameter to query document repository tree: documentId = " + null);
        }

        long documentId;
        try {
            documentId = Long.parseLong(idOp.get());
        } catch (NumberFormatException e) {
            log.error("Illegal parameter to query document repository tree: documentId = " + idOp.get(), e);
            return failWithMessage("Illegal parameter to query document repository tree: documentId = " + idOp.get());
        }

        Tuple2<List<DocumentCatalog>, List<DocumentChapter>> queryResult = documentRelationService.documentRepositoryCatalogAndChapter(documentId);
        ContainsCatalogCatalogBo rootSE = CatalogsTreeWalker.walk(queryResult.getT1(), queryResult.getT2());

        return allRightFromValue(rootSE.getCatalogs());
    }

}