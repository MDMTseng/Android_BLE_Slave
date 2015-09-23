



var callBKFuncx=null;
var InterblueScope=null;
//var InterBluApp = angular.module('InterBlu', ['ngAnimate']);
var InterBluApp = angular.module('InterBlu', []);

InterBluApp.controller('MainController', ['$scope', '$timeout', '$http',
function($scope, $timeout, $http) {
	InterblueScope=$scope;
	$scope.BTRECVData ="";
	$scope.Str = "";
	$scope.BlutoothDevs = [];
	$scope.SelectABlutoothDev = function(BlutoothDev) {

		$scope.BlutoothDevs = ["Loading"];

		if (( typeof Android) == "undefined") {
			$timeout(function() {
				$scope.BlutoothDevs = ["No Android"];
			}, 1000);
		} else {
			Android.Link2BTDev(BlutoothDev);
		}

	};

	$scope.BLEScan = function() {
		if (( typeof Android) == "undefined") {
			$scope.BlutoothDevs = ["No Android", "No Android1", "No Androi2", "No Androisdd", "No Androsdaasdfid"];
			return;
		}
		$scope.BlutoothDevs = ["!!Loading!!"];
		Android.BLEScan();
	};

	$scope.callBKFuncx={};
	$scope.callBKFuncx.BLEScanEvent = function(msg) {
			$scope.BlutoothDevs = JSON.parse(msg);
	};

	callBKFuncx=$scope.callBKFuncx;

	$scope.SetProfile = function(filePath) {
		$http.get(filePath).
			success(function(data, status, headers, config) {
				var dataObj={};
				dataObj.type="gattProfileJson";
				dataObj.data=data;
				Android.SendMsg2BT(JSON.stringify(dataObj));
				//console.log( data);
			}).
			error(function(data, status, headers, config) {
			  console.log( status);
			  // log error
			});
	};

	$scope.Renew_Cache = function() {
		console.log("Renew_CacheRenew_Cache");
		Android.Renew_Cache();
	};
	$scope.serverStatus = null;
	$scope.SetUpBTServer = function() {
		$scope.serverStatus = Android.SetUpBTServer();
		
		$timeout(function() {$scope.serverStatus  =null;}, 1000);
	};
	$scope.RECVData = function(msg) {

		console.log(msg);
		var dataObj = JSON.parse(msg);

		dataObj.rawData = window.atob(dataObj.base64Data);
		dataObj.base64Data=null;
		$scope.AppendRECVData(dataObj);
    };
	var BTRECVDataArr=[];
	$scope.AppendRECVData = function(dataObj) {
		BTRECVDataArr.push(dataObj.uuid32b.toString(16)+">>"+dataObj.rawData.length);
		if(BTRECVDataArr.length>100)
			BTRECVDataArr.shift();
		//$scope.BTRECVData.push(msg);
		$scope.BTRECVData=BTRECVDataArr.join("\n");
		
	};
	$scope.SendMsg2BT = function(msg) {
	};
	$scope.SendMsg2BT2 = function(CH,msg) {
		var uuid32b=parseInt(CH, 16);
		var base64Data=window.btoa(msg);
		var dataObj={uuid32b,base64Data};
		Android.SendMsg2BT(JSON.stringify(dataObj));
	};


}]).directive('ngEnter', function() {
	return function(scope, element, attrs) {
		element.bind("keydown keypress", function(event) {
			if (event.which === 13) {
				scope.$apply(function() {
					scope.$eval(attrs.ngEnter);
				});

				event.preventDefault();
			}
		});
	};
});


function AndroidCall(msg) {
	if(InterblueScope==null)return;
	InterblueScope.$apply(function(scope) {
		scope.RECVData(msg);
	});
}

