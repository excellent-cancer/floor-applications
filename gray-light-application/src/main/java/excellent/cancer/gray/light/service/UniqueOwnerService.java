package excellent.cancer.gray.light.service;

import excellent.cancer.gray.light.error.UniqueOwnerException;
import excellent.cancer.gray.light.jdbc.ReactiveJdbc;
import excellent.cancer.gray.light.jdbc.entities.Owner;
import excellent.cancer.gray.light.jdbc.entities.OwnerProject;
import excellent.cancer.gray.light.jdbc.repositories.OwnerProjectRepository;
import excellent.cancer.gray.light.jdbc.repositories.OwnerRepository;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static perishing.constraint.treasure.chest.CollectionsTreasureChest.asList;

/**
 * 定义关于所属者的相关服务，例如：项目，介绍等等
 *
 * @author XyParaCrim
 */
@Service
@CommonsLog
public final class UniqueOwnerService {

    @Getter
    private final Owner owner;

    @Getter
    private final OwnerRepository ownerRepository;

    @Getter
    private final OwnerProjectRepository projectRepository;

    /**
     * 不仅在构造函数中设置必要的参数，而且会查询出唯一的owner，并且将其设置到全局变量中
     *
     * @param ownerRepository   owner jdbc repository
     * @param projectRepository project jdbc repository
     * @throws UniqueOwnerException 如果不能够成功初始化唯一owner
     */
    @Autowired
    public UniqueOwnerService(OwnerRepository ownerRepository, OwnerProjectRepository projectRepository) {
        this.ownerRepository = ownerRepository;
        this.projectRepository = projectRepository;
        this.owner = UniqueOwnerException.extractOwnerRequireUniqueOwner(asList(ownerRepository.findAll()));
        GlobalFinalVariables.set(Owner.class, this.owner);
    }

    /**
     * 返回owner的所有项目
     *
     * @return 返回一个包含项目列表的flux
     */
    public Mono<List<OwnerProject>> projects() {
        return Mono.fromFuture(projectRepository.findByOwnerId(owner.getId()));
    }

    /**
     * 查询指定Id的项目
     *
     * @param projectId 项目Id
     * @return 返回包含项目操作的flux
     */
    public Mono<Optional<OwnerProject>> project(Long projectId) {
        return ReactiveJdbc.reactive(() -> projectRepository.findByOwnerIdAndId(owner.getId(), projectId));
    }

    public Mono<Boolean> existsProject(Long projectId) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> projectRepository.existsById(projectId)));
    }

    public Mono<OwnerProject> addProject(OwnerProject ownerProject) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> projectRepository.save(ownerProject)));
    }

    public Mono<Void> removeProject(OwnerProject project) {
        return Mono.fromFuture(CompletableFuture.runAsync(() -> projectRepository.delete(project)));
    }

}
