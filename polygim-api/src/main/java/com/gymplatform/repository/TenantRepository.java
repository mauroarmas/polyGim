package com.gymplatform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository {
    <T> void persist(T entity);
    <T> Optional<T> find(Class<T> type, UUID id);
    <T> List<T> list(Class<T> type, int offset, int limit);
    <T> long count(Class<T> type);
}
