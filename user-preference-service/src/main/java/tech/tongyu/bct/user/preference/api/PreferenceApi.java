package tech.tongyu.bct.user.preference.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.user.preference.dto.PreferenceDTO;
import tech.tongyu.bct.user.preference.service.PreferenceService;

import java.util.Arrays;
import java.util.List;

@Service
public class PreferenceApi {
    PreferenceService preferenceService;

    @Autowired
    public PreferenceApi(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @BctMethodInfo(
            description = "检查一个用户的偏好是否存在",
            retDescription = "用户的偏好是否存在",
            retName = "true or false",
            service = "user-preference-service"
    )
    public Boolean prefPreferenceExist(@BctMethodArg(description = "用户名") String userName) {
        return preferenceService.getPreference(userName).isPresent();
    }

    @BctMethodInfo(
            description = "获取一个用户的偏好",
            retDescription = "用户偏好",
            retName = "PreferenceDTO",
            returnClass = PreferenceDTO.class,
            service = "user-preference-service"
    )
    public PreferenceDTO prefPreferenceGetByUser(@BctMethodArg(description = "用户名") String userName) {
        return preferenceService.getPreference(userName)
                .orElseThrow(() -> new IllegalArgumentException(String.format("用户%s的preference未定义", userName)));
    }

    @BctMethodInfo(description = "创建一个用户的偏好",
            retDescription = "创建的用户偏好",
            retName = "PreferenceDTO",
            returnClass = PreferenceDTO.class,
            service = "user-preference-service"
    )
    public PreferenceDTO prefPreferenceCreate(
            @BctMethodArg(description = "用户名") String userName,
            @BctMethodArg(description = "波动率曲面的标的物") List<String> volInstruments,
            @BctMethodArg(description = "分红曲线的标的物") List<String> dividendInstruments) {
        return preferenceService.createPreference(userName, volInstruments, dividendInstruments);
    }

    @BctMethodInfo(description = "向用户偏好添加波动率标的物",
            retDescription = "改动后的用户偏好",
            retName = "PreferenceDTO",
            returnClass = PreferenceDTO.class,
            service = "user-preference-service"
    )
    public PreferenceDTO prefPreferenceVolInstrumentAdd(
            @BctMethodArg(description = "用户名") String userName,
            @BctMethodArg(description = "波动率曲面的标的物") String volInstrument) {
        return preferenceService.addVolInstruments(userName, Arrays.asList(volInstrument));
    }

    @BctMethodInfo(description = "向用户偏好添加分红曲线标的物",
            retDescription = "改动后的用户偏好",
            retName = "PreferenceDTO",
            returnClass = PreferenceDTO.class,
            service = "user-preference-service"
    )
    public PreferenceDTO prefPreferenceDividendInstrumentAdd(
            @BctMethodArg(description = "用户名") String userName,
            @BctMethodArg(description = "分红曲线标的物") String dividendInstrument) {
        return preferenceService.addDividendInstruments(userName, Arrays.asList(dividendInstrument));
    }

    @BctMethodInfo(description = "从用户偏好删除波动率标的物",
            retDescription = "改动后的用户偏好",
            retName = "PreferenceDTO",
            returnClass = PreferenceDTO.class,
            service = "user-preference-service"
    )
    public PreferenceDTO prefPreferenceVolInstrumentDelete(
            @BctMethodArg(description = "用户名") String userName,
            @BctMethodArg(description = "波动率曲面的标的物") String volInstrument) {
        return preferenceService.deleteVolInstruments(userName, Arrays.asList(volInstrument));
    }

    @BctMethodInfo(description = "从用户偏好删除分红曲线标的物",
            retDescription = "改动后的用户偏好",
            retName = "PreferenceDTO",
            returnClass = PreferenceDTO.class,
            service = "user-preference-service"
    )
    public PreferenceDTO prefPreferenceDividendInstrumentDelete(
            @BctMethodArg(description = "用户名") String userName,
            @BctMethodArg(description = "分红曲线标的物") String dividendInstrument) {
        return preferenceService.deleteDividendInstruments(userName, Arrays.asList(dividendInstrument));
    }
}
