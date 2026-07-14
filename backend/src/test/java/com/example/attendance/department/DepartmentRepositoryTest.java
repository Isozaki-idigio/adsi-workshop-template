package com.example.attendance.department;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository repository;

    @Test
    @DisplayName("部門を保存して取得できる")
    void save_and_findById() {
        var department = Department.builder()
                .name("ユニットテスト部_" + System.nanoTime())
                .build();

        var saved = repository.save(department);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("部門名で検索できる")
    void findByName_existingName_returnsDepartment() {
        var uniqueName = "検索対象部_" + System.nanoTime();
        repository.save(Department.builder().name(uniqueName).build());

        Optional<Department> found = repository.findByName(uniqueName);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(uniqueName);
    }

    @Test
    @DisplayName("存在しない部門名で検索するとempty")
    void findByName_nonExistingName_returnsEmpty() {
        Optional<Department> found = repository.findByName("存在しない部");

        assertThat(found).isEmpty();
    }
}
