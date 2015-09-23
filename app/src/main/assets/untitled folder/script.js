function AndroidCall(msg) {
	console.log(msg);
	angular.element(document.getElementById('MainController')).scope().$apply(function(scope) {
		//scope.saveCtrlData();
		scope.Str = msg;
	});
}


function Android_BTRECV(msg) {
	console.log(msg);
	angular.element(document.getElementById('MainController')).scope().$apply(function(scope) {

		scope.AppendRECVData(msg);

	});
}



/*function Android_feedBluetoothNode(msg)
{
console.log(msg);
angular.element(document.getElementById('MainController')).scope().$apply(function(scope) {
//scope.saveCtrlData();

var result = JSON.parse(msg);
scope.BlutoothDevs=result;
});
}*/

var callBKFuncx=null;
//var InterBluApp = angular.module('InterBlu', ['ngAnimate']);
var InterBluApp = angular.module('InterBlu', []);

InterBluApp.controller('MainController', ['$scope', '$timeout', '$http',
function($scope, $timeout, $http) {

	$scope.BTRECVData ="";
	$scope.Str = "";
	$scope.BlutoothDevs = [];
	$scope.SelectABlutoothDev = function(BlutoothDev) {

		$scope.BlutoothDevs = ["Loading"];

		if (( typeof Android) == "undefined") {
			$timeout(function() {

				$scope.BlutoothDevs = ["No Androirrd"];

			}, 1000);
		} else {
			Android.Link2BTDev(BlutoothDev);
		}

	}

	$scope.BLEScan = function() {
		if (( typeof Android) == "undefined") {
			$scope.BlutoothDevs = ["No Android", "No Android1", "No Androi2", "No Androisdd", "No Androsdaasdfid"];
			return;
		}
		$scope.BlutoothDevs = ["!!Loading!!"];
		Android.BLEScan();
	}

	$scope.callBKFuncx={};
	$scope.callBKFuncx.BLEScanEvent = function(msg) {
		$scope.$apply(function () {
			$scope.BlutoothDevs = JSON.parse(msg);
        });
	}

	callBKFuncx=$scope.callBKFuncx;



	$scope.Renew_Cache = function() {
		console.log("Renew_CacheRenew_Cache");
		Android.Renew_Cache();
	}
	$scope.serverStatus = null;
	$scope.SetUpBTServer = function() {
		$scope.serverStatus = Android.SetUpBTServer();
		
		$timeout(function() {$scope.serverStatus  =null;}, 1000);
	}
	var BTRECVDataArr=[]; 
	
	$scope.AppendRECVData = function(msg) {
		var tmpFilter=$scope.RECVFilter;
		if(tmpFilter!=null&&tmpFilter.length>0&&msg.indexOf($scope.RECVFilter)==-1 )return;
		BTRECVDataArr.push(">>"+msg);
		if(BTRECVDataArr.length>100)
			BTRECVDataArr.shift();
		//$scope.BTRECVData.push(msg);
		$scope.BTRECVData=BTRECVDataArr.join("\n");
		
	}
	$scope.SendMsg2BT = function(msg) {
		Android.SendMsg2BT(msg);
	}
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