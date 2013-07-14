'''
Made a bug in a call to writelines. Fixing it up now.
'''

import argparse
import gzip
import json


if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('nyt_posts_file', metavar = 'nyt-posts-file')
		parser.add_argument('nyt_dest_file', metavar = 'nyt-dest-file')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	posts_handle = gzip.open(parsed.nyt_posts_file, 'rb')
	dest_handle = gzip.open(parsed.nyt_dest_file, 'wb')

	posts_str = '[' + '},{'.join(posts_handle.read().split('}{')) + ']'
	posts = json.loads(posts_str)

	post_lines = map(lambda p : json.dumps(p), posts)
	post_lines = map(lambda p : p + '\n', post_lines)

	dest_handle.writelines(post_lines)