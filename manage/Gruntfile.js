module.exports = function(grunt){

	//load npm task
	grunt.loadNpmTasks('grunt-shell');
  	grunt.loadNpmTasks('grunt-contrib-copy');
  	grunt.loadNpmTasks('grunt-contrib-clean');
  	grunt.loadNpmTasks('grunt-contrib-watch');
  	grunt.loadNpmTasks('grunt-contrib-uglify');
  	grunt.loadNpmTasks('grunt-contrib-concat');
  	grunt.loadNpmTasks('grunt-contrib-connect');
  	grunt.loadNpmTasks('grunt-string-replace');
  	grunt.loadNpmTasks('grunt-contrib-rename');
  	grunt.loadNpmTasks("grunt-remove-logging");
  	grunt.loadNpmTasks('grunt-karma');
	grunt.loadNpmTasks('grunt-angular-templates');
	grunt.loadNpmTasks('grunt-typescript');

	//project configuration
	grunt.initConfig({
		pkg : grunt.file.readJSON('package.json'),
		config: {
			tempversion: "?v0.07",
			SOURCE: "source",
			DEST_DEV: "deploy/dev",
			DEST_STAGING: "deploy/staging"
		},
		uglify: {
			options: {
				banner: '/*! <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> */\n',
				// You can specify identifiers to leave untouched with an except array in the mangle options.
				mangle: {
        			except: ['jQuery', 'AngularJS']
      			}
			},
			website: {
				src: ['source/modules/website/*.js','!source/modules/website/module.js','source/modules/website/module.js'],
				dest: 'source/modules/website/website.min.js'
			}
		},
		clean: {
			options: { force: true },
			build: ['build/**'],
			dev: ['<%= config.DEST_DEV %>/**'],
			staging: ['<%= config.DEST_STAGING %>/**'],
			removecommonlive: ['<%= config.DEST_STAGING %>/modules/common/common.js']
		},
		concat: {
			backend: {
				src: ['source/modules/backend/rest.js',
				'source/modules/backend/data.js'],
				dest: 'source/modules/backend/backend.sum.js'
			},
			website: {
				src: ['source/modules/website/*.js',
				'!source/modules/website/module.js',
				'!source/modules/website/website.sum.js',
				'source/modules/website/module.js'],
				dest: 'source/modules/website/website.sum.js'
			},
			user: {
				src: ['source/modules/user/*.js',
				'!source/modules/user/module.js',
				'!source/modules/user/user.sum.js',
				'source/modules/user/module.js'],
				dest: 'source/modules/user/user.sum.js'
			},
			campaign: {
				src: ['source/modules/campaign/*.js',
				'!source/modules/campaign/module.js',
				'!source/modules/campaign/campaign.sum.js',
				'source/modules/campaign/module.js'],
				dest: 'source/modules/campaign/campaign.sum.js'
			},
			admin: {
				src: ['source/modules/admin/*.js',
				'!source/modules/admin/module.js',
				'!source/modules/admin/admin.sum.js',
				'source/modules/admin/module.js'],
				dest: 'source/modules/admin/admin.sum.js'
			},
			report: {
				src: ['source/modules/report/*.js',
				'!source/modules/report/module.js',
				'!source/modules/report/report.sum.js',
				'source/modules/report/module.js'],
				dest: 'source/modules/report/report.sum.js'
			},
			root: {
				src: ['source/modules/root/*.js',
				'!source/modules/root/module.js',
				'!source/modules/root/root.sum.js',
				'source/modules/root/module.js'],
				dest: 'source/modules/root/root.sum.js'
			},
			agency: {
				src: ['source/modules/agency/*.js',
				'!source/modules/agency/module.js',
				'!source/modules/agency/agency.sum.js',
				'source/modules/agency/module.js'],
				dest: 'source/modules/agency/agency.sum.js'
			},
			role: {
				src: ['source/modules/role/*.js',
				'!source/modules/role/module.js',
				'!source/modules/role/role.sum.js',
				'source/modules/role/module.js'],
				dest: 'source/modules/role/role.sum.js'
			},
			user_role: {
				src: ['source/modules/user_role/*.js',
				'!source/modules/user_role/module.js',
				'!source/modules/user_role/user_role.sum.js',
				'source/modules/user_role/module.js'],
				dest: 'source/modules/user_role/user_role.sum.js'
			},
			article: {
				src: ['source/modules/article/*.js',
				'!source/modules/article/module.js',
				'!source/modules/article/article.sum.js',
				'source/modules/article/module.js'],
				dest: 'source/modules/article/article.sum.js'
			},
			system: {
				src: ['source/modules/system/*.js',
				'!source/modules/system/module.js',
				'!source/modules/system/system.sum.js',
				'source/modules/system/module.js'],
				dest: 'source/modules/system/system.sum.js'
			}
		},
		copy: {
			build: {
				files: [
					{expand: true, cwd: "<%= config.SOURCE %>", src: ['common/*.js', 'modules/*/*.sum.js', 'modules/common/*.js','resource/**', 'services/*.js', '*.htm', '*.js', 'template/**'], dest: 'build/'}
				]
			},
		  	dev: {
			    files: [
			      // includes files within path and its sub-directories
			      {expand: true, cwd: "build", src: ['**'], dest: '<%= config.DEST_DEV %>/'}
			    ]
		  	},
		  	staging: {
		  		files: [
		  			{expand: true, cwd: "build", src: ['**'], dest: '<%= config.DEST_STAGING %>/'}
		  		]
		  	},
		  	staginghtml: {
		  		files: [
		  			{expand: true, cwd: "<%= config.DEST_STAGING %>", src: ['default.htm'], dest: '<%= config.DEST_STAGING %>/', rename: function (dest, src){
		  				return dest + 'staging.htm';
		  			}}
		  		]
		  	}
		},
		removelogging: {
		    module: {
		      src: "build/modules/**/*.js" // Each file will be overwritten with the output!
		    },
		    service: {
		      src: "build/services/**/*.js" // Each file will be overwritten with the output!
		    }
	    },
		rename: {
			commonlive: {
			    files: [
			        	{src: ['<%= config.DEST_STAGING %>/modules/common/common_live.js'], dest: '<%= config.DEST_STAGING %>/modules/common/common.js'},
			        ]
			}
		},
		'string-replace': {
			versioning: {
				files: {
					'build/modules/admin/admin.sum.js': 'build/modules/admin/admin.sum.js',
					'build/modules/agency/agency.sum.js': 'build/modules/agency/agency.sum.js',
					'build/modules/campaign/campaign.sum.js': 'build/modules/campaign/campaign.sum.js',
					'build/modules/article/article.sum.js': 'build/modules/article/article.sum.js',
					'build/modules/report/report.sum.js': 'build/modules/report/report.sum.js',
					'build/modules/root/root.sum.js': 'build/modules/root/root.sum.js',
					'build/modules/user/user.sum.js': 'build/modules/user/user.sum.js',
					'build/modules/website/website.sum.js': 'build/modules/website/website.sum.js',
					'build/modules/system/system.sum.js': 'build/modules/system/system.sum.js',
				}, 
				options: {
					replacements: [{
						pattern: /templateUrl:.*\.html\"/ig,
						replacement: function (match, p1, offset, string) {
			              	return match.substring(0, match.length - 1) + grunt.config.get('config.tempversion') + '"';
			            }
					}]
				}
			},
			mapping: {
				files: {
					'build/modules/backend/backend.sum.js': 'build/modules/backend/backend.sum.js',
					'build/modules/admin/admin.sum.js': 'build/modules/admin/admin.sum.js',
					'build/modules/agency/agency.sum.js': 'build/modules/agency/agency.sum.js',
					'build/modules/campaign/campaign.sum.js': 'build/modules/campaign/campaign.sum.js',
					'build/modules/article/article.sum.js': 'build/modules/article/article.sum.js',
					'build/modules/common/common.js': 'build/modules/common/common.js',
					'build/modules/report/report.sum.js': 'build/modules/report/report.sum.js',
					'build/modules/role/role.sum.js': 'build/modules/role/role.sum.js',
					'build/modules/root/root.sum.js': 'build/modules/root/root.sum.js',
					'build/modules/user/user.sum.js': 'build/modules/user/user.sum.js',
					'build/modules/user_role/user_role.sum.js': 'build/modules/user_role/user_role.sum.js',
					'build/modules/website/website.sum.js': 'build/modules/website/website.sum.js',
					'build/modules/system/system.sum.js': 'build/modules/system/system.sum.js',
					'build/services/apiservices.js': 'build/services/apiservices.js',
					'build/services/services.js': 'build/services/services.js',
					'build/common/basecontroller.js': 'build/common/basecontroller.js',
					'build/common/common.js': 'build/common/common.js',
					'build/common/scopes.js': 'build/common/scopes.js',
					'build/common/utils.js': 'build/common/utils.js',
					'build/app.js': 'build/app.js',
					'build/directives.js': 'build/directives.js'
				},
				options: {
			      	replacements: [{
					        pattern: /\/\/\# sourceMappingURL=.*\.js\.map/ig,
					        replacement: ''
				      	}]
			    }
			},
			staging: {
		    	files: {
		    		'<%= config.DEST_STAGING %>/modules/common/common_staging.js': '<%= config.DEST_STAGING %>/modules/common/common.js'
		    	},
			    options: {
			      	replacements: [{
					        pattern: 'http://dev.api.adtima.vn/rest/misc/upload',
					        replacement: 'http://staging.api.adtimaserver.vn/rest/misc/upload'
				      	}, {
				      		pattern: 'http://dev.api.adtima.vn/rest',
				      		replacement: 'http://staging.api.adtimaserver.vn/rest'
				      	}, {
					        pattern: 'http://dev.adtima.vn',
					        replacement: 'http://staging.static.adtima.vn'
				      	}]
			    }
		  	},
		  	live: {
		  		files: {
		    		'<%= config.DEST_STAGING %>/modules/common/common_live.js': '<%= config.DEST_STAGING %>/modules/common/common.js'
		    	},
			    options: {
			      	replacements: [{
					        pattern: 'http://dev.api.adtima.vn/rest/misc/upload',
					        replacement: 'http://api.adtima.vn/rest/misc/upload'
				      	}, {
				      		pattern: 'http://dev.api.adtima.vn/rest',
				      		replacement: 'http://api.adtima.vn/rest'
				      	}, {
					        pattern: 'http://dev.adtima.vn',
					        replacement: 'http://static.adtima.vn'
				      	}]
			    }
		  	},
		  	
		  	staginghtml: {
		    	files: {
		    		'<%= config.DEST_STAGING %>/staging.htm': '<%= config.DEST_STAGING %>/staging.htm'
		    	},
			    options: {
			      	replacements: [{
					        pattern: 'modules/common/common.js',
					        replacement: 'modules/common/common_staging.js'
				      	}]
			    }
		  	}
		},
		karma: {
	      	unit: {
		        configFile: '123Click/test/karma-unit.conf.js',
		        autoWatch: false,
		        singleRun: true
	      	},
	      	unit_auto: {
	        	configFile: '123Click/test/karma-unit.conf.js'
	      	},
	      	midway: {
	        	configFile: '123Click/test/karma-midway.conf.js',
	        	autoWatch: false,
	        	singleRun: true
	      	},
	      	midway_auto: {
	        	configFile: '123Click/test/karma-midway.conf.js'
	      	},
	      	e2e: {
	        	configFile: '123Click/test/karma-e2e.conf.js',
	        	autoWatch: false,
	        	singleRun: true
	      	},
	      	e2e_auto: {
	        	configFile: '123Click/test/karma-e2e.conf.js'
	      	}
	    },
	    watch: {
		  	website: {
			    files: ['source/modules/website/*.js'],
			    tasks: ['concat:website'],
			    options: {
			      	spawn: false
			    },
		  	},
		  	campaign: {
			    files: ['source/modules/campaign/*.js'],
			    tasks: ['concat:campaign'],
			    options: {
			      	spawn: false
			    },
		  	},
		  	user: {
			    files: ['source/modules/user/*.js'],
			    tasks: ['concat:user'],
			    options: {
			      	spawn: false
			    },
		  	},
			agency: {
			    files: ['source/modules/agency/*.js'],
			    tasks: ['concat:agency'],
				options: {
			      	spawn: false
			    },
			},
		  	admin: {
			    files: ['source/modules/admin/*.js'],
			    tasks: ['concat:admin'],
			    options: {
			      	spawn: false
			    }
		  	},
		  	report: {
			    files: ['source/modules/report/*.js'],
			    tasks: ['concat:report'],
			    options: {
			      	spawn: false
			    }
		  	},
		  	report: {
			    files: ['source/modules/system/*.js'],
			    tasks: ['concat:system'],
			    options: {
			      	spawn: false
			    }
		  	},
		},
		ngtemplates: {
			"123click": {
				options: {
					url:    function(url) { return url.replace("source/", "").replace(".html", ""); },
					htmlmin:  { 
						collapseWhitespace: true,
						removeComments: true,
						removeEmptyAttributes: true,
						minifyJS: true,
						minifyCSS: true
					}
				},
				src: "source/views/**/**.html",
				dest: "source/templates.js"
			}
		},
		typescript: {
		  base: {
			src: ['source/modules/**/*.ts', 'source/app.ts', 'source/directives.ts', 'source/common/*.ts'],
			dest: '/',
			options: {
			  module: 'amd',
			  target: 'es3',
			  sourceMap: true,
			  noLib: true,
			  comments: false,
			  watch: true
			}
		  }
		}
	});
	
	// Default task(s).
	grunt.registerTask('build:ts', ['typescript'])
	grunt.registerTask('default', ['concat', 'ngtemplates', 'typescript']);
	grunt.registerTask('build', ['concat', 'clean:build', 'copy:build', 'removelogging:module', 'removelogging:service', 'string-replace:versioning', 'ngtemplates', 'typescript']);
	grunt.registerTask('build:all', ['build:dev', 'build:staging']);
	grunt.registerTask('build:dev', ['build', 'string-replace:mapping','clean:dev','copy:dev']);
	grunt.registerTask('build:staging', ['build', 'string-replace:mapping', 'clean:staging', 'copy:staging', 'string-replace:staging', 'string-replace:live', 'copy:staginghtml', 'clean:removecommonlive', 'rename:commonlive', 'string-replace:staginghtml']);
	grunt.task.registerTask('checkconfig', 'Check configuration', function () {
		grunt.log.writeln(config.SOURCE);
	});
	grunt.registerTask('autotest',['karma:unit_auto','karma:midway_auto','karma:e2e_auto']) ;
	grunt.registerTask('autotest:unit',['karma:unit_auto']);
	grunt.registerTask('autotest:midway',['karma:midway_auto']);
	grunt.registerTask('autotest:e2e',['karma:e2e_auto']);

	grunt.registerTask('test',['karma:unit','karma:midway','karma:e2e']) ;
	grunt.registerTask('test:unit',['karma:unit']);
	grunt.registerTask('test:midway',['karma:midway']);
	grunt.registerTask('test:e2e',['karma:e2e']);

}
