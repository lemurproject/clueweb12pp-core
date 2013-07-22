'''
Routines to process the clueweb disqus dataset
'''

import argparse
import datetime
import gzip
import json
from dateutil.parser import parse


def iter_clueweb_posts(post_data_file):
	f = gzip.open(post_data_file, 'r')

	while True:
		try:
			yield json.loads(f.readline())
		except:
			break

	f.close()

def last_date(post_data_file):
	last_date = None

	for post in iter_clueweb_posts(post_data_file):
		last_date = post['createdAt']

	return parse(last_date)

def last_date_epoch(post_data_file):
	return (last_date(post_data_file) - datetime.datetime(1970, 1, 1)).total_seconds()

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('post_file', metavar = 'post-file', )
		parser.add_argument('--last-date-epoch', dest = 'last_date_epoch', default = False, action = 'store_true', help = 'Identify last epoch in posts file')
		parser.add_argument('--last-date', dest = 'last_date', default = False, action = 'store_true', help = 'Identify last epoch in posts file')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	if parsed.last_date_epoch:
		print last_date_epoch(parsed.post_file)

	elif parsed.last_date:
		print last_date(parsed.post_file)