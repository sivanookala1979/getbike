 function dropdown1(){
       $("#basePackageMinutes").val("");
       $("#basePackageAmount").val("");
       $("#basePackageKilometers").val("");
       $("#additionalPerKilometer").val("");
       $("#additionalPerMinute").val("");
         $("#fixedPriceAmount").val("");
        if(document.getElementById("fixedPrice").checked)
        {
                document.getElementById("hasBasePackage").checked = false;
                 $("#fixedAmount").show(); $("#additional").hide();
                 $("#basePackagecheck").hide(); $("#baseAmount").hide();
        } else {
                 $("#fixedAmount").hide();$("#additional").show();
                 $("#basePackagecheck").show();
        }

   }
    function dropdown2(){

       $("#fixedPriceAmount").val("");
        if(document.getElementById("hasBasePackage").checked)
        {
              document.getElementById("fixedPrice").checked = false;
             $("#fixedAmount").hide();
             $("#baseAmount").show();$("#additional").show();
        } else {
             $("#baseAmount").hide();$("#price").show();$("#additional").show();

        }
   }