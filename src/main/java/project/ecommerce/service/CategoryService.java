package project.ecommerce.service;

import project.ecommerce.dto.CategoryRequest;
import project.ecommerce.dto.CategoryResponse;
import project.ecommerce.entity.Category;
import project.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Categoria já existe.");
        }

        Category category = Category.builder().name(request.getName()).description(request.getDescription()).build();

        return toResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> findAllIncludingInactive() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse findById(Long id) {
        return toResponse(categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada.")));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada."));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return toResponse(categoryRepository.save(category));
    }


    public void delete(Long id) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada."));
        category.setActive(false);
        categoryRepository.save(category);
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setActive(category.getActive());
        return response;
    }
}
