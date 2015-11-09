var addedCustomAttributes = {};
var attributeCount = 0;
var removeCustomAttributes = {};

function validateInput(text, element, errorMsg){

    var elementId = element.attr('id');
    if(text == ""){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
        return false;
    }else{
        $('#label'+elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

function removeCustomAtrribute(count){

    var id = '#attributeName'+count;
    var key = $(id).val();
    delete addedCustomAttributes[key];
    $('#attribute'+count).remove();
}

function addToRemoveMap(count){

    removeCustomAttributes[$('#attributeName'+count).val()]=$('#attributeValue'+count).val();
    console.log("Removed map ");
    console.log(removeCustomAttributes);
}

function populateCustomerAttributes(attributesList){

    var attributes = attributesList;
    var tBody = $('#custom-attribute-tbody');

    if(attributes != null){
        $.each(attributes, function( index, value ) {
            ++ attributeCount;
            addCustomAtrributeInitially(tBody, attributeCount,index, value);
        });
    }
}

function addCustomAtrribute(element, count){

    var elementId = element.attr('id');
    element.parent().append(
        '<tr id="attribute'+count+'">'+
        '<td><div class="clear"></div></td>'+
        '<td><input type="text" id="attributeName'+count+'" name="attributeName'+count+'" placeholder="Attribute Name" onchange="customAttributeChange('+count+')" /></td>'+
        '<td><input type="text" id="attributeValue'+count+'" name="attributeValue'+count+'" placeholder="Value" onchange="customAttributeChange('+count+')" /></td>'+
        '<td class="delete_resource_td"><a  id="attibuteDelete'+count+'"  href="javascript:removeCustomAtrribute('+count+')"><i class="icon-trash"></i></a></td>'+
        '</tr>'
    );
}

function addCustomAtrributeInitially(element, count, name, value){

    var elementId = element.attr('id');
    element.parent().append(
        '<tr id="attribute'+count+'">'+
        '<td><div class="clear"></div></td>'+
        '<td><input type="text" id="attributeName'+count+'" name="attributeName'+count+'" onchange="customAttributeChange('+count+')" readonly/></td>'+
        '<td><input type="text" id="attributeValue'+count+'" name="attributeValue'+count+'" onchange="customAttributeChange('+count+')" readonly/></td>'+
        '<td class="delete_resource_td "><a  id="attibuteDelete'+count+'"  href="javascript:addToRemoveMap('+count+');removeCustomAtrribute('+count+');"><i class="icon-trash"></i></a></td>'+
        '</tr>'
    );

    $('#attributeName'+count).val(name);
    $('#attributeValue'+count).val(value);
}

function customAttributeChange(count){
    addedCustomAttributes[$('#attributeName'+count).val()]=$('#attributeValue'+count).val();
    console.log("Added Map " );
    console.log(addedCustomAttributes);
}

$(document).ready(function(){

    $('#add-tier-btn').on('click',function(){
        //validatet name
        var requiredMsg = $('#errorMsgRequired').val();
        var tierName = $('#tierName');
        var tierNameTxt = tierName.val();

        if(validateInput(tierNameTxt,tierName,requiredMsg) == false){
            return;
        }

        var requestCount = $('#requestCount');
        var requestCountTxt = requestCount.val();

        if(validateInput(requestCountTxt,requestCount,requiredMsg) == false){
            return;
        }

        var unitTime = $('#unitTime');
        var unitTimeTxt = unitTime.val();

        if(validateInput(unitTimeTxt,unitTime,requiredMsg) == false){
            return;
        }
        console.log(removeCustomAttributes);
        console.log(addedCustomAttributes);
    });

    $('#add-attribute-btn').on('click',function(){
        ++ attributeCount;
        console.log(" attribute count : "+ attributeCount);
        var tBody = $('#custom-attribute-tbody');
        addCustomAtrribute(tBody, attributeCount);
    });

});
