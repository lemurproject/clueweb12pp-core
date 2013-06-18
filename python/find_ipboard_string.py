'''
Find IPBoard String
'''

import argparse
import warc
from BeautifulSoup import BeautifulSoup
from heritrix import core



def handle_warc_file(warc_file):
	f = warc.open(warc_file)

	for record in f:
		if not core.is_response_record(record):
			continue
		soup = BeautifulSoup(record.payload.read())
		
		if soup.text.find('IP.Board') >= 0:
			yield record.url

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('job_name', metavar = 'job-name')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	import sys
	reload(sys)
	sys.setdefaultencoding("utf-8")

	for warc_file in core.job_warc_files(parsed.job_name):
		for link in handle_warc_file(warc_file):
			print link
			#sys.stdout.flush()