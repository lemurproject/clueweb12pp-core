'''
Code to track disqus progress
'''

import argparse
import clueweb_disqus_process
import datetime
import numpy as np
import time
import matplotlib

matplotlib.use('Agg')

import matplotlib.pyplot as plt

from matplotlib import dates

def plot_curve(num_posts, last_date, title, csv_file, png_file):
	records = [(int(record.split(',')[0]), int(record.split(',')[1])) for record in open(csv_file, 'r').readlines()]

	new_record = '%(timestamp)d,%(num_posts)s' % {'timestamp' : int(time.mktime(datetime.datetime.now().timetuple())), 'num_posts': str(num_posts)}

	records.append((int(time.mktime(datetime.datetime.now().timetuple())), num_posts))

	with open(csv_file, 'a+') as csv_file:
		csv_file.write(new_record + '\n')

	dts = map(datetime.datetime.fromtimestamp, [record[0] for record in records])
	fds = dates.date2num(dts) # converted

	# matplotlib date format object
	hfmt = dates.DateFormatter('%m/%d %H:%M')

	y1 = [record[1] for record in records]

	fig = plt.figure()
	ax = fig.add_subplot(111)
	ax.plot(fds, y1)

	ax.set_xlim(-1,100)
	ax.xaxis.set_major_locator(dates.MinuteLocator())
	ax.xaxis.set_major_formatter(hfmt)
	plt.xticks(rotation='vertical')
	plt.xlabel('Timestamp')
	plt.ylabel('Number of posts')
	plt.subplots_adjust(bottom=.3)
	plt.savefig(png_file)

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('disqus_post_files', metavar = 'disqus-post-files', nargs = '+', help = 'Disqus post files for a month')
		parser.add_argument('csv_file', metavar = 'csv-file')
		parser.add_argument('png_file', metavar = 'png-file')
		parser.add_argument('month')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	num_posts = 0
	last_date = datetime.datetime(1970, 1, 1)

	for post_file in parsed.disqus_post_files:
		num_posts += clueweb_disqus_process.num_posts(post_file)
		post_file_last_date = clueweb_disqus_process.last_date(post_file)

		if post_file_last_date > last_date:
			last_date = post_file_last_date

		print 'Processed: %s' % post_file

	plot_curve(num_posts, last_date, parsed.month, parsed.csv_file, parsed.png_file)

	with open(parsed.month + '_dates.txt', 'w') as month_handle:
		month_handle.write('Dates: ' + str(last_date))