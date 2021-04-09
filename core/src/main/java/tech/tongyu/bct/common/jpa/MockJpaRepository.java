package tech.tongyu.bct.common.jpa;


import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class MockJpaRepository<T extends HasUuid> implements JpaRepository<T, UUID> {

    protected final List<T> data;

    public MockJpaRepository(List<T> data) {
        this.data = new ArrayList<>();
        this.data.addAll(data);
    }

    @Override
    public List<T> findAll() {
        return data;
    }

    @Override
    public List<T> findAll(Sort sort) {
        throw new UnsupportedOperationException("findAll does not support sorting in mock");
    }

    @Override
    public List<T> findAllById(Iterable<UUID> iterable) {
        Set<UUID> set = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toSet());
        return data.stream().filter(d -> set.contains(d.getUuid())).collect(Collectors.toList());
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).map(e -> save(e)).collect(Collectors.toList());
    }

    @Override
    public Optional<T> findById(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return data.stream().filter(d -> uuid.equals(d.getUuid())).findFirst();
    }

    @Override
    public boolean existsById(UUID uuid) {
        if (uuid == null) return false;
        return data.stream().anyMatch(e -> uuid.equals(e.getUuid()));
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("findAll does not support paging in mock");
    }

    @Override
    public long count() {
        return data.size();
    }

    @Override
    public void deleteById(UUID uuid) {
        if (uuid != null) {
            data.removeAll(data.stream().filter(d -> uuid.equals(d.getUuid())).collect(Collectors.toList()));
        }
    }

    @Override
    public void delete(T entity) {
        deleteById(entity.getUuid());
    }

    @Override
    public void deleteAll(Iterable<? extends T> iterable) {
        Set<UUID> set = StreamSupport.stream(iterable.spliterator(), false).map(d -> ((T) d).getUuid()).collect(Collectors.toSet());
        data.removeAll(data.stream().filter(d -> set.contains(d.getUuid())).collect(Collectors.toList()));
    }

    @Override
    public void deleteAll() {
        data.clear();
    }

    @Override
    public <S extends T> S save(S entity) {
        return findById(entity.getUuid())
                .map(e -> {
                    delete(entity);
                    data.add(entity);
                    return entity;
                })
                .orElseGet(() -> {
                    entity.setUuid(UUID.randomUUID());
                    data.add(entity);
                    return entity;
                });
    }

    @Override
    public void flush() {
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(Iterable<T> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public T getOne(UUID uuid) {
        return findById(uuid).get();
    }


    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("findOne does not support Example in mock");
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("findAll does not support Example in mock");
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("findAll does not support Example or Sort in mock");
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("findAll does not support Example or Pageable in mock");
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        throw new UnsupportedOperationException("count does not support Example in mock");
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("exists does not support Example in mock");
    }
}

