/**数据验证（表单验证）*/
module.exports={error:'',check:function(data,rule){for(var i=0;i<rule.length;i++){if(!rule[i].checkType){return!0}
if(!rule[i].name){return!0}
if(!rule[i].errorMsg){return!0}
if(!data[rule[i].name]){this.error=rule[i].errorMsg
return!1}
switch(rule[i].checkType){case 'string':var reg=new RegExp('^.{'+rule[i].checkRule+'}$')
if(!reg.test(data[rule[i].name])){this.error=rule[i].errorMsg
return!1}
break
case 'int':var reg=new RegExp('^(-[1-9]|[1-9])[0-9]{'+rule[i].checkRule+'}$')
if(!reg.test(data[rule[i].name])){this.error=rule[i].errorMsg
return!1}
break
break
case 'between':if(!this.isNumber(data[rule[i].name])){this.error=rule[i].errorMsg
return!1}
var minMax=rule[i].checkRule.split(',')
minMax[0]=Number(minMax[0])
minMax[1]=Number(minMax[1])
if(data[rule[i].name]>minMax[1]||data[rule[i].name]<minMax[0]){this.error=rule[i].errorMsg
return!1}
break
case 'betweenD':var reg=/^-?[1-9][0-9]?$/
if(!reg.test(data[rule[i].name])){this.error=rule[i].errorMsg
return!1}
var minMax=rule[i].checkRule.split(',')
minMax[0]=Number(minMax[0])
minMax[1]=Number(minMax[1])
if(data[rule[i].name]>minMax[1]||data[rule[i].name]<minMax[0]){this.error=rule[i].errorMsg
return!1}
break
case 'betweenF':var reg=/^-?[0-9][0-9]?.+[0-9]+$/
if(!reg.test(data[rule[i].name])){this.error=rule[i].errorMsg
return!1}
var minMax=rule[i].checkRule.split(',')
minMax[0]=Number(minMax[0])
minMax[1]=Number(minMax[1])
if(data[rule[i].name]>minMax[1]||data[rule[i].name]<minMax[0]){this.error=rule[i].errorMsg
return!1}
break
case 'same':if(data[rule[i].name]!=rule[i].checkRule){this.error=rule[i].errorMsg
return!1}
break
case 'notsame':if(data[rule[i].name]==rule[i].checkRule){this.error=rule[i].errorMsg
return!1}
break
case 'email':var reg=/^\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/
if(!reg.test(data[rule[i].name])){this.error=rule[i].errorMsg
return!1}
break
case 'phoneno':var reg=/^1[0-9]{10,10}$/
if(!reg.test(data[rule[i].name])){this.error=rule[i].errorMsg
return!1}
break
case 'zipcode':var reg=/^[0-9]{6}$/
if(!reg.test(data[rule[i].name])){this.error=rule[i].errorMsg
return!1}
break
case 'reg':var reg=new RegExp(rule[i].checkRule)
if(!reg.test(data[rule[i].name])){this.error=rule[i].errorMsg
return!1}
break
case 'in':if(rule[i].checkRule.indexOf(data[rule[i].name])==-1){this.error=rule[i].errorMsg
return!1}
break
case 'notnull':if(data[rule[i].name]==null||data[rule[i].name].length<1){this.error=rule[i].errorMsg
return!1}
break}}
return!0},isNumber:function(checkVal){var reg=/^-?[1-9][0-9]?.?[0-9]*$/
return reg.test(checkVal)}}
