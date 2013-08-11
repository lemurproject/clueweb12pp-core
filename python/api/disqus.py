'''
Discussions from disqus
'''

import argparse
import datetime
import gzip
import json
import time
from dateutil import parser
from disqusapi import DisqusAPI, Paginator

OUTPUT_FILE = None
OUTPUT_HANDLE = None
CLUEWEB_START_EPOCH = 1325394000
DISQUS_LOG = 'posts.log'

def parse_creation_time(date_str):
	return parser.parse(date_str)

def write_post(post_str):
	OUTPUT_HANDLE.write(post_str + '\n')

def log(datum):
	with open(DISQUS_LOG, 'w+') as disqus_log_handle:
		disqus_log_handle.write(str(datum))


if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('secret_key', metavar = 'secret-key', help = 'Disqus API secret key')
		parser.add_argument('public_key', metavar = 'public-key', help = 'Disqus API public key')
		parser.add_argument('--start-date', dest = 'start_date', type = int, default = CLUEWEB_START_EPOCH, help = 'Start at a custom date')
		parser.add_argument('--log', dest = 'log', help = 'Where to store update')
		parser.add_argument('--dest', dest = 'dest', help = 'Where to store the posts')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	DISQUS_LOG = parsed.log
	api = DisqusAPI(parsed.secret_key, parsed.public_key)

	OUTPUT_FILE = parsed.dest
	OUTPUT_HANDLE = open(OUTPUT_FILE, 'ab')

	since_arg = parsed.start_date

	last_read_time = since_arg

	posts_downloaded = 0

	while True:
		paginator = Paginator(api.threads.list, since = last_read_time, order = 'asc')
		try:
			for result in paginator():
				result_str = json.dumps(result)
				write_post(result_str)
				posts_downloaded += 1
				last_read_time = (parse_creation_time(result['createdAt']) - datetime.datetime(1970,1,1)).total_seconds()
				log(posts_downloaded)
		except Exception as e:
			if e.code == 13:
				time.sleep(2000)
			elif e.code == 15:
				last_read_time += 1
				continue
			import ipdb
			ipdb.set_trace()
			