'''
Generate reports for Threads
'''

import gzip
import json


# 1 GZ file per month

JAN_GZ_FILE = '/bos/tmp19/spalakod/clueweb12pp/jobs/disqus/posts2.gz'
FEB_GZ_FILE = '/bos/tmp19/spalakod/clueweb12pp/jobs/disqus/posts_feb.gz'
MAR_GZ_FILE = '/bos/tmp19/spalakod/clueweb12pp/jobs/disqus/posts_mar.gz'
APR_GZ_FILE = '/bos/tmp19/spalakod/clueweb12pp/jobs/disqus/posts_apr.gz'
MAY_GZ_FILE = '/bos/tmp19/spalakod/clueweb12pp/jobs/disqus/posts_may.gz'
JUN_GZ_FILE = '/bos/tmp19/spalakod/clueweb12pp/jobs/disqus/posts_jun.gz'

def most_recent_date(gz_file):
	f = gzip.open(gz_file, 'rb')

	to_return = None

	while True:
		try:
			new_line = f.readline()

			if not new_line:
				return

			parsed = json.loads(new_line)
			to_return = parsed['createdAt']

		except:
			break

	return to_return

with open('/bos/www/htdocs/spalakod/posts/disqus_dates_report.txt', 'w+') as disqus_report_handle:
	disqus_report_handle.write('January Job: ' + str(most_recent_date(JAN_GZ_FILE)) + '\n')
	disqus_report_handle.write('February Job: ' + str(most_recent_date(FEB_GZ_FILE)) + '\n')
	disqus_report_handle.write('March Job: ' + str(most_recent_date(MAR_GZ_FILE)) + '\n')
	disqus_report_handle.write('April Job: ' + str(most_recent_date(APR_GZ_FILE)) + '\n')
	disqus_report_handle.write('May Job: ' + str(most_recent_date(MAY_GZ_FILE)) + '\n')
	disqus_report_handle.write('June Job: ' + str(most_recent_date(JUN_GZ_FILE)) + '\n')
