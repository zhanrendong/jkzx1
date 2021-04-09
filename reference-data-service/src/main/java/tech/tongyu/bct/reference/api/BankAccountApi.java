package tech.tongyu.bct.reference.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.reference.dto.BankAccountDTO;
import tech.tongyu.bct.reference.service.BankAccountService;

import java.util.List;

@Service
public class BankAccountApi {

    private BankAccountService bankAccountService;

    @Autowired
    public BankAccountApi(BankAccountService bankAccountService){
        this.bankAccountService = bankAccountService;
    }

    @BctMethodInfo(
            description = "交易对手银行账户信息创建更新",
            retDescription = "银行账户信息",
            retName = "BankAccountDTO",
            returnClass = BankAccountDTO.class,
            service = "reference-data-service"
    )
    public BankAccountDTO refBankAccountSave(
            @BctMethodArg(name = "uuid", description = "唯一标识,更新操作必填,新增操作无需", required = false) String uuid,
            @BctMethodArg(name = "bookName", description = "开户行名称") String bankName,
            @BctMethodArg(name = "legalName", description = "交易对手") String legalName,
            @BctMethodArg(name = "bankAccount", description = "银行账户") String bankAccount,
            @BctMethodArg(name = "bankAccountName", description = "银行账户名称") String bankAccountName,
            @BctMethodArg(name = "paymentSystemCode", description = "支付系统行号", required = false) String paymentSystemCode){
        if (StringUtils.isBlank(bankName)){
            throw new IllegalArgumentException("请输入开户行bankName");
        }
        if (StringUtils.isBlank(legalName)){
            throw new IllegalArgumentException("请输入交易对手legalName");
        }
        if (StringUtils.isBlank(bankAccount)){
            throw new IllegalArgumentException("请输入银行账户bankAccount");
        }
        if (StringUtils.isBlank(bankAccountName)){
            throw new IllegalArgumentException("请输入账户名称bankAccountName");
        }

        BankAccountDTO bankAccountDto = new BankAccountDTO(uuid, bankName, legalName, bankAccount, bankAccountName, paymentSystemCode);
        if (StringUtils.isBlank(uuid)){
            return bankAccountService.createBankAccount(bankAccountDto);
        }
        return bankAccountService.updateBankAccount(bankAccountDto);

    }

    @BctMethodInfo(
            description = "交易对手银行账户删除操作",
            retDescription = "是否删除成功",
            service = "reference-data-service"
    )
    public Boolean refBankAccountDel(
            @BctMethodArg(name = "uuid", description = "银行账户唯一标识") String uuid){
        if (StringUtils.isBlank(uuid)){
            throw new IllegalArgumentException("请输入银行账户唯一标识uuid");
        }
        bankAccountService.deleteBankAccount(uuid);
        return true;
    }

    @BctMethodInfo(
            description = "模糊搜索银行账号",
            retDescription = "银行账号列表",
            retName = "List<String>",
            service = "reference-data-service"
    )
    public List<String> refSimilarBankAccountList(
            @BctMethodArg(name = "similarBankAccount", description = "银行账户") String similarBankAccount){
        return bankAccountService.listBySimilarBankAccount(similarBankAccount);
    }

    @BctMethodInfo(
            description = "模糊搜索银行账户名称信息",
            retDescription = "银行账户名称列表",
            retName = "List<String>",
            service = "reference-data-service"
    )
    public List<String> refSimilarAccountNameList(
            @BctMethodArg(name = "similarAccountName", description = "银行账户名称") String similarAccountName){
        return bankAccountService.listBySimilarAccountName(similarAccountName);
    }

    @BctMethodInfo(
            description = "查询交易对手银行账户信息",
            retDescription = "银行账户信息列表",
            retName = "List<BankAccountDTO>",
            returnClass = BankAccountDTO.class,
            service = "reference-data-service"
    )
    public List<BankAccountDTO> refBankAccountSearch(
            @BctMethodArg(name = "legalName", description = "交易对手", required = false) String legalName,
            @BctMethodArg(name = "bankAccount", description = "银行账户", required = false) String bankAccount,
            @BctMethodArg(name = "bankAccountName", description = "银行账户名称", required = false) String bankAccountName){
        BankAccountDTO bankAccountDto = new BankAccountDTO();
        bankAccountDto.setLegalName(killBlank(legalName));
        bankAccountDto.setBankAccount(killBlank(bankAccount));
        bankAccountDto.setBankAccountName(killBlank(bankAccountName));

        return bankAccountService.searchBankAccounts(bankAccountDto);
    }


    private String killBlank(String s) {
        return StringUtils.isBlank(s) ? null : s;
    }

}
