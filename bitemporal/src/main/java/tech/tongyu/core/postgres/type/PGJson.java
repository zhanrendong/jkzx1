package tech.tongyu.core.postgres.type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Component
public class PGJson implements UserType {

    private static ObjectMapper objectMapper;

    @Autowired
    private void setObjectMapper(ObjectMapper objectMapper) {
        PGJson.objectMapper = objectMapper;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            try {
                String json = objectMapper.writeValueAsString(value);
                st.setObject(index, json, Types.OTHER);
            } catch (Exception e) {
                e.printStackTrace();
                st.setNull(index, Types.OTHER);
            }
        }
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        PGobject o = (PGobject) rs.getObject(names[0]);
        if (o == null)
            return objectMapper.createObjectNode();
        if (o.getValue() != null) {
            try {
                return objectMapper.readTree(o.getValue());
            } catch (Exception e) {
                e.printStackTrace();
                return objectMapper.createObjectNode();
            }
        } else {
            return objectMapper.createObjectNode();
        }
    }

    @Override
    public Object deepCopy(Object orig) throws HibernateException {
        if (orig == null)
            return null;
        if (!(orig instanceof JsonNode))
            return null;
        return ((JsonNode) orig).deepCopy();
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        Object copy = deepCopy(value);

        if (copy instanceof Serializable) {
            return (Serializable) copy;
        }

        throw new SerializationException(String.format("Cannot serialize '%s', %s is not Serializable.",
                value, value.getClass()), null);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object orig, Object target, Object owner) throws HibernateException {
        return deepCopy(orig);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        if (x == null)
            return 0;
        return x.hashCode();
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return ObjectUtils.nullSafeEquals(x, y);
    }

    @Override
    public Class<?> returnedClass() {
        return JsonNode.class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{9000};
    }
}