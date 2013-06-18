'''
Preparing to distribute reddit across current crawl infrastructure
'''

import argparse
import gzip
import json


def read_posts(posts_file):
	f = gzip.open(posts_file)

	for new_line in f:
		line_dict = json.loads(new_line)
		
		if line_dict['data']['num_comments'] > 0:
			print line_dict['data']['id']

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('reddit_posts_list' ,metavar = 'reddit-posts-list', help = 'List to the posts.gz file on reddit')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	read_posts(parsed.reddit_posts_list)