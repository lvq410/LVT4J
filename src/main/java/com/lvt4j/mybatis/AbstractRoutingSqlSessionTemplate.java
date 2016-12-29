package com.lvt4j.mybatis;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.mybatis.spring.SqlSessionUtils.closeSqlSession;
import static org.mybatis.spring.SqlSessionUtils.getSqlSession;
import static org.mybatis.spring.SqlSessionUtils.isSqlSessionTransactional;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.support.PersistenceExceptionTranslator;


/**
 * 重写mybatis的SqlSessionTemplate<br>
 * 以支持分布式事务<br>
 * 只需实现{@link #getSqlSessionFactory()}方法
 * @author LV
 */
public abstract class AbstractRoutingSqlSessionTemplate extends SqlSessionTemplate {
	 
    private final ExecutorType executorType;
    private final SqlSession sqlSessionProxy;
    private final PersistenceExceptionTranslator exceptionTranslator;
 
    public AbstractRoutingSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
    }
 
    public AbstractRoutingSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
        this(sqlSessionFactory, executorType, new MyBatisExceptionTranslator(sqlSessionFactory.getConfiguration()
                .getEnvironment().getDataSource(), true));
    }
 
    public AbstractRoutingSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
            PersistenceExceptionTranslator exceptionTranslator) {
        super(sqlSessionFactory, executorType, exceptionTranslator);
        this.executorType = executorType;
        this.exceptionTranslator = exceptionTranslator;
        this.sqlSessionProxy = (SqlSession) newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class[] { SqlSession.class }, 
                new SqlSessionInterceptor());
    }
 
    public abstract SqlSessionFactory getSqlSessionFactory();
 
    @Override
    public Configuration getConfiguration() {
        return getSqlSessionFactory().getConfiguration();
    }
 
    public ExecutorType getExecutorType() {
        return executorType;
    }
 
    public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
        return exceptionTranslator;
    }
 
    public <T> T selectOne(String statement) {
        return sqlSessionProxy.<T> selectOne(statement);
    }
 
    public <T> T selectOne(String statement, Object parameter) {
        return sqlSessionProxy.<T> selectOne(statement, parameter);
    }
 
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return sqlSessionProxy.<K, V> selectMap(statement, mapKey);
    }
 
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return sqlSessionProxy.<K, V> selectMap(statement, parameter, mapKey);
    }
 
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return sqlSessionProxy.<K, V> selectMap(statement, parameter, mapKey, rowBounds);
    }
 
    public <E> List<E> selectList(String statement) {
        return sqlSessionProxy.<E> selectList(statement);
    }
 
    public <E> List<E> selectList(String statement, Object parameter) {
        return sqlSessionProxy.<E> selectList(statement, parameter);
    }
 
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return sqlSessionProxy.<E> selectList(statement, parameter, rowBounds);
    }
 
    @SuppressWarnings("rawtypes")
    public void select(String statement, ResultHandler handler) {
        sqlSessionProxy.select(statement, handler);
    }
 
    @SuppressWarnings("rawtypes")
    public void select(String statement, Object parameter, ResultHandler handler) {
        sqlSessionProxy.select(statement, parameter, handler);
    }
 
    @SuppressWarnings("rawtypes")
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }
 
    public int insert(String statement) {
        return sqlSessionProxy.insert(statement);
    }
 
    public int insert(String statement, Object parameter) {
        return sqlSessionProxy.insert(statement, parameter);
    }
 
    public int update(String statement) {
        return sqlSessionProxy.update(statement);
    }
 
    public int update(String statement, Object parameter) {
        return sqlSessionProxy.update(statement, parameter);
    }
 
    public int delete(String statement) {
        return sqlSessionProxy.delete(statement);
    }
 
    public int delete(String statement, Object parameter) {
        return sqlSessionProxy.delete(statement, parameter);
    }
 
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
    }
 
    public void commit() {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }
 
    public void commit(boolean force) {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }
 
    public void rollback() {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }
 
    public void rollback(boolean force) {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }
 
    public void close() {
        throw new UnsupportedOperationException("Manual close is not allowed over a Spring managed SqlSession");
    }
 
    public void clearCache() {
        sqlSessionProxy.clearCache();
    }
 
    public Connection getConnection() {
        return sqlSessionProxy.getConnection();
    }
 
    public List<BatchResult> flushStatements() {
        return sqlSessionProxy.flushStatements();
    }
 
    /**
     * Proxy needed to route MyBatis method calls to the proper SqlSession got from Spring's Transaction Manager It also
     * unwraps exceptions thrown by {@code Method#invoke(Object, Object...)} to pass a {@code PersistenceException} to
     * the {@code PersistenceExceptionTranslator}.
     */
    private class SqlSessionInterceptor implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final SqlSession sqlSession = getSqlSession(
                    getSqlSessionFactory(),
                    executorType, 
                    exceptionTranslator);
            try {
                Object result = method.invoke(sqlSession, args);
                if (!isSqlSessionTransactional(sqlSession, getSqlSessionFactory())) {
                    // force commit even on non-dirty sessions because some databases require
                    // a commit/rollback before calling close()
                    sqlSession.commit(true);
                }
                return result;
            } catch (Throwable t) {
                Throwable unwrapped = unwrapThrowable(t);
                if (exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                    Throwable translated = exceptionTranslator
                        .translateExceptionIfPossible((PersistenceException) unwrapped);
                    if (translated != null) {
                        unwrapped = translated;
                    }
                }
                throw unwrapped;
            } finally {
                closeSqlSession(sqlSession, getSqlSessionFactory());
            }
        }
    }
 
}