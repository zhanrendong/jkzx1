package tech.tongyu.core.postgres.type;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Component
public class PGTsTzRange implements UserType {

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            try {
                st.setObject(index, value.toString(), Types.OTHER);
            } catch (Exception e) {
                st.setNull(index, Types.OTHER);
            }
        }
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        PGobject o = (PGobject) rs.getObject(names[0]);
        if (o != null && o.getValue() != null) {
            try {
                return TsTzRange.from(o.getValue());
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Object deepCopy(Object orig) throws HibernateException {
        if (orig == null)
            return null;
        if (!(orig instanceof TsTzRange))
            return null;
        return ((TsTzRange) orig).deepCopy();
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
        return TsTzRange.class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{9001};
    }
}
