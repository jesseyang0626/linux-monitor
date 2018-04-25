$(function(){
	var app = new Vue({
	  el: '#app',
	  data: {
	    loading: true,
	    doing: false,
	    list:[]
	  },
	  created:function(){
		  this.checkAllFromCache()
	  },
	  methods:{
		  checkAll:function(){
			  var self = this
			  $.ajax({
				  url:'/command/checkAll',
				  beforeSend:function(){
					  self.loading=true
				  },
				  complete:function(){
					  self.loading=false
				  },
				  success:function(data){
					  if(data.success){
						  self.list=data.t
					  }
					  location.reload();
				//	  console.log(data)
				  }
			  })
		  },
		  checkAllFromCache:function(){
			  var self = this
			  $.ajax({
				  url:'/command/checkAllFromCache',
				  beforeSend:function(){
					  self.loading=true
				  },
				  complete:function(){
					  self.loading=false
				  },
				  success:function(data){
					  if(data.success){
						  self.list=data.t
					  }
				//	  console.log(data)
				  }
			  })
		  },
		  doCommand:function(e,ip,serviceName,action){
			  var dom  = event.currentTarget
			  var self = this
			  var requestUrl = ''
				  
			  Ewin.confirm({ message: "确认操作吗？" }).on(function (e) {
				  if (!e) {
					  return;
				  }else{
					  switch(action){
					  	case 'start':
					  		requestUrl = '/command/serviceStart'
					  		break
					  	case 'stop':
					  		requestUrl = '/command/serviceStop'
						  	break
					  	case 'restart':
					  		requestUrl = '/command/serviceRestart'
						  	break
					  }
					  
					  $.ajax({
						  url:requestUrl,
						  data:{
							  ip:ip,
							  serviceName:serviceName
						  },
						  beforeSend:function(){
				//			  dom.setAttribute("disabled", "disabled");
							  self.doing=true
							  self.list=[]
						  },
						  complete:function(){
							  self.doing=false
							  self.checkAll()
				//			  dom.removeAttribute("disabled");
						  },
						  success:function(data){
							  if(data.success){
						//		  self.list=data.t
							  }
						  }
					  })
				  }
			  });
			 
		  },
		  updateWar:function(){
			  
			  var self = this;
			  var fileDir = $("#fileDir").val()
			  if(fileDir!=null && fileDir !=""){
				  $.ajax({ 
					  url : "/command/updateWar?fileDir="+fileDir, 
					  type : 'get', 
					  beforeSend:function(){
						  $('#fileModal').modal('hide')
						//  console.log("正在进行，请稍候");
						  self.doing = true
						  
					  },
					  success : function(data) { 
						  self.doing = false
						  if(data.success){
							  Ewin.alert("更新成功")
						  }else{
							  Ewin.alert(data.msg)
						  }
					  }, 
					  error : function(responseStr) { 
						  Ewin.alert("系统错误，请重新检查各服务状态")
					  } 
					  });
			  }else{
				  Ewin.alert("请填写存放mss.war的路径，在文件夹复制粘贴即可");
			  }
			  
			  
		  },
		  updateSQL:function(){
			  var self = this;
			  Ewin.confirm({ message: "请确认已经备份所有数据库！ 确认后不可恢复。继续请点击确定" }).on(function (e) {
				  if (!e) {
					  return;
				  }else{
					  var sqlDir = $("#sqlDir").val()
					  if(sqlDir!=null && sqlDir !=""){
						  $.ajax({ 
							  url : "/command/updateSQL?fileDir="+sqlDir, 
							  type : 'get', 
							  beforeSend:function(){
								  $('#sqlModal').modal('hide')
								//  console.log("正在进行，请稍候");
								  self.doing = true
								  
							  },
							  success : function(data) { 
								  self.doing = false
								  if(data.success){
									  Ewin.alert("更新成功")
								  }else{
									  Ewin.alert(data.msg)
								  }
							  }, 
							  error : function(responseStr) { 
								  Ewin.alert("系统错误，请重新检查各服务状态")
							  } 
							  });
					  }else{
						  Ewin.alert("请填写存放三个数据库文件的路径，在文件夹复制粘贴即可");
					  }
				  }
				  })
			
			   
		  },
		  serviceStart:function(e,ip,serviceName){
			  console.log(ip+serviceName)
			  console.log(event.currentTarget)
			  var dom  = event.currentTarget
			 
			  var self = this
			  $.ajax({
				  url:'/command/serviceStart',
				  data:{
					  ip:ip,
					  serviceName:serviceName
				  },
				  beforeSend:function(){
					  dom.setAttribute("disabled", "disabled");
				  },
				  complete:function(){
					  self.checkAll()
		//			  dom.removeAttribute("disabled");
				  },
				  success:function(data){
					  if(data.success){
				//		  self.list=data.t
					  }
				  }
			  })
		  },
		  serviceStop:function(event,ip,serviceName){
			  console.log(ip+serviceName)
			  var self = this
			  var dom  = event.currentTarget
			  $.ajax({
				  url:'/command/serviceStop',
				  data:{
					  ip:ip,
					  serviceName:serviceName
				  },
				  beforeSend:function(){
					  dom.setAttribute("disabled", "disabled");
				  },
				  complete:function(){
					  self.checkAll()
				  },
				  success:function(data){
					  if(data.success){
			//			  self.list=data.t
					  }
				  }
			  })
		  },
		  serviceRestart:function(e,ip,serviceName){
			  console.log(ip+serviceName)
			  var self = this
			  var dom  = event.currentTarget
			  $.ajax({
				  url:'/command/serviceRestart',
				  data:{
					  ip:ip,
					  serviceName:serviceName
				  },
				  beforeSend:function(){
					  dom.setAttribute("disabled", "disabled");
				  },
				  complete:function(){
					  self.checkAll()
					  dom.removeAttribute("disabled")
				  },
				  success:function(data){
					  if(data.success){
				//		  self.list=data.t
					  }
				  }
			  })
		  }
	  }
	})
})
