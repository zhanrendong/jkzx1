package tech.tongyu.bct.user.preference.mock;

import tech.tongyu.bct.common.jpa.MockJpaRepository;
import tech.tongyu.bct.user.preference.dao.dbo.Preference;
import tech.tongyu.bct.user.preference.dao.repo.intel.PreferenceRepo;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MockPreferenceRepository extends MockJpaRepository<Preference> implements PreferenceRepo {
    public MockPreferenceRepository() {
        super(new LinkedList<>());
    }

    @Override
    public Optional<Preference> findByUserName(String userName) {
        return data.stream().filter(p -> p.getUserName().equals(userName)).findAny();
    }
}
