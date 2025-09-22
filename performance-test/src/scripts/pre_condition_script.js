const fiscalCodeEC = process.env.FISCAL_CODE_EC;

//DEFINE RECEIPT DATA
const createPositionForReceipt = async () => {

    var body = getBody();

    var responseMDB = await getMDB(fiscalCodeEC, body);
    this.payResponse = await getPayPosition(fiscalCodeEC, correctNav);
    this.dueDate = this.response?.data?.dueDate;
    var payBody = {
      "paymentDate": this.response?.data?.insertedDate,
      "paymentMethod": this.response?.data?.paymentMethod,
      "pspCompany": this.response?.data?.pspCompany
      "idReceipt": this.response?.data?.idReceipt,
      "fee": this.response?.data?.fee
    }
    await payReceipt(fiscalCodeEC, this.responseMDB?.data?.nav, payBody);


};

createPositionForReceipt().then(resp => {
  console.info("PRE_CONDITION_SCRIPT COMPLETED");
}) ;
