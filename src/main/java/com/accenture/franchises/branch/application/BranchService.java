package com.accenture.franchises.branch.application;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.BranchProduct;
import com.accenture.franchises.branch.domain.BranchRepositoryPort;
import com.accenture.franchises.branch.domain.TopProduct;
import com.accenture.franchises.common.exception.ConflictException;
import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.domain.FranchiseRepositoryPort;
import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.domain.ProductRepositoryPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BranchService {

    private static final String TYPE = "Branch";

    private final BranchRepositoryPort branches;
    private final FranchiseRepositoryPort franchises;
    private final ProductRepositoryPort products;

    public BranchService(
            BranchRepositoryPort branches,
            FranchiseRepositoryPort franchises,
            ProductRepositoryPort products) {
        this.branches = branches;
        this.franchises = franchises;
        this.products = products;
    }

    @Transactional
    public Branch addBranch(UUID franchiseId, String name) {
        Franchise franchise = getExistingFranchise(franchiseId);
        if (branches.existsByNameInFranchise(name, franchise.getId())) {
            throw new DuplicateNameException(TYPE, name);
        }
        return branches.save(Branch.create(name, franchise.getId()));
    }

    public Branch findById(UUID branchId) {
        return getExistingBranch(branchId);
    }

    @Transactional
    public Branch rename(UUID branchId, String name) {
        Branch branch = getExistingBranch(branchId);
        if (!branch.getName().equalsIgnoreCase(name)
                && branches.existsByNameInFranchise(name, branch.getFranchiseId())) {
            throw new DuplicateNameException(TYPE, name);
        }

        return branches.save(branch.rename(name));
    }

    @Transactional
    public void delete(UUID branchId) {
        getExistingBranch(branchId);
        branches.delete(branchId);
    }

    @Transactional
    public BranchProduct addProduct(UUID branchId, UUID productId, int stock) {
        Branch branch = getExistingBranch(branchId);
        Product product = getExistingProduct(productId);
        if (branches.findProductStock(branch.getId(), product.getId()).isPresent()) {
            throw new ConflictException("Product %s is already added to branch %s".formatted(productId, branchId));
        }
        return branches.addProduct(branch.getId(), product.getId(), stock);
    }

    @Transactional
    public void removeProduct(UUID branchId, UUID productId) {
        getExistingBranch(branchId);
        getExistingProduct(productId);

        if (branches.findProductStock(branchId, productId).isEmpty()) {
            throw new ResourceNotFoundException("Product %s is not linked to branch %s".formatted(productId, branchId));
        }

        branches.removeProduct(branchId, productId);
    }

    @Transactional
    public BranchProduct updateStock(UUID branchId, UUID productId, int stock) {
        getExistingBranch(branchId);
        getExistingProduct(productId);

        if (branches.findProductStock(branchId, productId).isEmpty()) {
            throw new ResourceNotFoundException("Product %s is not linked to branch %s".formatted(productId, branchId));
        }

        return branches.updateStock(branchId, productId, stock);
    }

    public List<TopProduct> topProductsPerBranch(UUID franchiseId) {
        getExistingFranchise(franchiseId);
        return branches.findTopProductsPerBranch(franchiseId);
    }

    private Branch getExistingBranch(UUID branchId) {
        return branches.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private Product getExistingProduct(UUID productId) {
        return products.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private Franchise getExistingFranchise(UUID franchiseId) {
        return franchises.findById(franchiseId)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise", franchiseId));
    }
}